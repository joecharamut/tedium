package rocks.spaghetti.tedium.script;

import com.oracle.truffle.js.runtime.JSException;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.graalvm.polyglot.*;
import org.jetbrains.annotations.NotNull;
import rocks.spaghetti.tedium.ClientEntrypoint;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.Util;
import rocks.spaghetti.tedium.core.AbstractInventory;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.core.InteractionManager;

import javax.script.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

public class ScriptEnvironment {
    private static ScriptEnvironment instance = null;
    private final ScriptExecutor scriptExecutor = new ScriptExecutor();

    private ScriptEnvironment() {
        if (FakePlayer.get() == null) {
            throw new IllegalStateException("ScriptEnvironment APIs require FakePlayer");
        }
    }

    public static ScriptEnvironment getInstance() {
        if (instance == null) instance = new ScriptEnvironment();
        return instance;
    }

    public void execResource(String resourceLocation) {
        execString(Util.getResourceAsString(resourceLocation));
    }

    public void execFile(File scriptFile) {
        if (!scriptFile.isFile()) return;

        scriptExecutor.execute(() -> {
            try (Scope scope = new Scope()) {
                scope.eval(scriptFile);
            } catch (Exception e) {
                scriptExceptionHandler(e);
            } finally {
                ClientEntrypoint.setFakePlayerState(false);
            }
        });

        if (!scriptExecutor.isAlive()) scriptExecutor.start();
    }

    private void execString(String code) {
        if (code.isEmpty()) {
            Log.warn("Not running empty string!");
            return;
        }

        scriptExecutor.execute(() -> {
            try (Scope scope = new Scope()) {
                scope.eval(code, "<eval>");
            } catch (Exception e) {
                scriptExceptionHandler(e);
            } finally {
                ClientEntrypoint.setFakePlayerState(false);
            }
        });

        if (!scriptExecutor.isAlive()) scriptExecutor.start();
    }

    private void scriptExceptionHandler(Exception e) {
        if (e instanceof PolyglotException) {
            PolyglotException polyException = (PolyglotException) e;
            Log.error("Script Exception:");
            Log.catching(e);

            if (polyException.isGuestException()) {
                try {
                    Class<?> exceptionClass = Class.forName("com.oracle.truffle.polyglot.PolyglotExceptionImpl");

                    Field implField = PolyglotException.class.getDeclaredField("impl");
                    implField.setAccessible(true);
                    Object impl = implField.get(e);

                    Field guestExceptionField = exceptionClass.getDeclaredField("exception");
                    guestExceptionField.setAccessible(true);
                    Throwable guestException = (Throwable) guestExceptionField.get(impl);
                    if (guestException instanceof JSException) {
                        JSException jse = (JSException) guestException;
                        SourceSection location = polyException.getSourceLocation();

                        MutableText errorInfo = new LiteralText(jse.getErrorType().name())
                                .append(" (").append(jse.getCause().getMessage()).append(")")
                                .append(" caused by ").append(location.getSource().getName())
                                .append(":").append(Integer.toString(location.getStartLine()))
                                .append(" `").append(location.getCharacters().toString()).append("`")
                                .formatted(Formatting.RED);
                        ClientEntrypoint.sendClientMessage(errorInfo);
                    }
                } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException ex) {
                    Log.error("Exception in exception handler:");
                    Log.catching(ex);
                }
            }
        } else {
            ClientEntrypoint.sendClientMessage(new LiteralText("Unhandled Exception: " + e.getClass()).formatted(Formatting.RED));
            Log.error("Unhandled Exception: {}", e.getClass());
            Log.catching(e);
        }
    }

    private static class ScriptExecutor extends Thread implements Executor {
        private final Queue<Runnable> runQueue = new ArrayDeque<>();

        public ScriptExecutor() {
            setName("Script Execution Thread");
        }

        @Override
        public void execute(@NotNull Runnable runnable) {
            runQueue.add(runnable);
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (!runQueue.isEmpty()) {
                    runQueue.poll().run();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    this.interrupt();
                }
            }
        }
    }

    private static class Scope implements Closeable {
        private static final String LANGUAGE = "js";
        private final GraalJSScriptEngine engine;

        public Scope() {
            engine = GraalJSScriptEngine.create(null,
                    Context.newBuilder(LANGUAGE)
                    .allowHostAccess(HostAccess.ALL)
                    .allowHostClassLookup(s -> true)
                    .option("js.ecmascript-version", "2021")
            );
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("Sys", new SysApi());
            bindings.put("Data", new DataApi());
            bindings.put("Minecraft", new MinecraftApi());
        }

        public Object eval(String source, String name) {
            return engine.getPolyglotContext().eval(Source.newBuilder(LANGUAGE, source, name).buildLiteral());
        }

        public Object eval(File file) throws IOException {
            return engine.getPolyglotContext().eval(Source.newBuilder(LANGUAGE, file).build());
        }

        @Override
        public void close() {
            engine.close();
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class SysApi {
            public void log(Object msg) {
                Log.info("[Script] {}", msg);
            }
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class DataApi {
            public Object getProp(String key) {
                return null;
            }
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class MinecraftApi {
            private static void waitWhile(BooleanSupplier condition) {
                while (condition.getAsBoolean() && !Thread.currentThread().isInterrupted()) {
                    Util.sleep(100);
                }
            }

            public void sendMessage(String message) {
                ClientEntrypoint.sendClientMessage(message);
            }

            public boolean isAiDisabled() {
                return FakePlayer.get().isAiDisabled();
            }

            public void goToBlock(int x, int y, int z) {
                FakePlayer player = FakePlayer.get();
                BlockPos dest = new BlockPos(x, y, z);

                int range = 1;
                player.setPositionTarget(dest, range);

                waitWhile(() -> player.squaredDistanceTo(Util.Vec3iToVec3d(dest)) > range+2);
            }

            public List<ItemStack> openContainerAt(int x, int y, int z) {
                FakePlayer player = FakePlayer.get();
                BlockPos blockPos = new BlockPos(x, y, z);

                Block block = player.world.getBlockState(blockPos).getBlock();
                if (!block.is(Blocks.CHEST)) return Collections.emptyList();
                InteractionManager.pushEvent(new InteractionManager.BlockInteractEvent(blockPos));

                waitWhile(() -> ClientEntrypoint.getOpenContainer() == null);
                return ClientEntrypoint.getOpenContainer().getExternalStacks();
            }

            public boolean quickMoveStack(int slot) {
                AbstractInventory container = ClientEntrypoint.getOpenContainer();
                if (container == null) return false;

                container.quickMoveStack(slot);
                return true;
            }

            public void closeContainer() {
                AbstractInventory container = ClientEntrypoint.getOpenContainer();
                if (container == null) return;

                container.close();
            }

            public BlockPos getPos() {
                return FakePlayer.get().getBlockPos();
            }

            public BlockState getBlockStateAt(int x, int y, int z) {
                return FakePlayer.get().world.getBlockState(new BlockPos(x, y, z));
            }
        }
    }
}
