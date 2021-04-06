package rocks.spaghetti.tedium.script;

import com.oracle.truffle.js.runtime.JSException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.graalvm.polyglot.*;
import org.jetbrains.annotations.Nullable;
import rocks.spaghetti.tedium.ClientEntrypoint;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.Util;
import rocks.spaghetti.tedium.core.AbstractInventory;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.core.InteractionManager;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
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

    @Nullable
    public Source getRunningSource() {
        return scriptExecutor.currentSource;
    }

    public void interruptRunningScript() {
        scriptExecutor.interrupt();
    }

    public void execFile(File scriptFile) {
        if (!scriptFile.isFile()) return;
        scriptExecutor.execute(Scope.toSource(scriptFile));
    }

    private void execString(String code) {
        if (code.isEmpty()) return;
        scriptExecutor.execute(Scope.toSource(code, "<eval>"));
    }

    private static class ScriptExecutor {
        private Source currentSource = null;
        private Scope currentScope = null;
        private Thread scriptThread;

        public void execute(Source source) {
            if (currentSource != null) {
                Log.error("Already executing a script!");
                return;
            }

            currentSource = source;
            scriptThread = new Thread(() -> {
                try (Scope scope = new Scope()) {
                    currentScope = scope;
                    scope.eval(source);
                } catch (Exception e) {
                    scriptExceptionHandler(e);
                } finally {
                    ClientEntrypoint.setFakePlayerState(false);
                    currentSource = null;
                    currentScope = null;
                }
            });
            scriptThread.setName("Script Execution Thread");
            scriptThread.start();
        }

        private void interrupt() {
            currentScope.context.close(true);
            scriptThread.interrupt();
        }

        private static void scriptExceptionHandler(Exception e) {
            if (e instanceof PolyglotException) {
                PolyglotException polyException = (PolyglotException) e;

                if (polyException.isCancelled() || polyException.isInterrupted()) {
                    Log.info("Script has been interrupted");
                    ClientEntrypoint.sendClientMessage(new TranslatableText("text.tedium.scriptInterrupted"));
                    return;
                }

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

                            MutableText errorInfo = new LiteralText(jse.getErrorType().name());

                            if (jse.getCause() != null) {
                                errorInfo.append(" (").append(jse.getCause().getMessage()).append(")");
                            } else {
                                errorInfo.append(" (").append(jse.getRawMessage()).append(")");
                            }

                            errorInfo
                                    .append(" caused by ").append(location.getSource().getName())
                                    .append(" line ").append(Integer.toString(location.getStartLine()))
                                    .append(": `").append(location.getCharacters().toString()).append("`")
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
                Log.error("Unhandled Exception:");
                Log.catching(e);
            }
        }
    }

    private static class Scope implements Closeable {
        private static final String LANGUAGE = "js";
        private final Context context;

        public Scope() {
            context = Context.newBuilder(LANGUAGE)
                    .allowHostAccess(HostAccess.ALL)
                    .allowHostClassLookup(className -> true)
                    .option("js.ecmascript-version", "2021")
                    .build();
            Value bindings = context.getBindings(LANGUAGE);
            bindings.putMember("Sys", new SysApi());
            bindings.putMember("Util", new ScriptUtil());
            bindings.putMember("Minecraft", new MinecraftApi());
        }

        public static Source toSource(String code, String name) {
            try {
                return Source.newBuilder(LANGUAGE, code, name).build();
            } catch (IOException e) {
                return null;
            }
        }

        public static Source toSource(File file) {
            try {
                return Source.newBuilder(LANGUAGE, file).build();
            } catch (IOException e) {
                return null;
            }
        }

        public Value eval(Source source) {
            return context.eval(source);
        }

        @Override
        public void close() {
            context.close();
        }

        private static void waitWhile(BooleanSupplier condition) {
            while (condition.getAsBoolean() && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class SysApi {
            public void log(Object msg) {
                Log.info("[Script] {}", msg);
            }
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class ScriptUtil {
            public BlockPos blockPosOf(int x, int y, int z) {
                return new BlockPos(x, y, z);
            }

            public Vec3d vec3dOf(double x, double y, double z) {
                return new Vec3d(x, y, z);
            }
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class MinecraftApi {
            public void sendMessage(Object obj) {
                if (obj == null) obj = "null";
                ClientEntrypoint.sendClientMessage(obj.toString());
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

            public void faceDirection(String direction) {
                Direction dir = Direction.byName(direction);
                if (dir == null) throw new IllegalArgumentException(String.format("Invalid direction \"%s\"", direction));
                FakePlayer player = FakePlayer.get();
                player.yaw = dir.asRotation();
                waitWhile(() -> player.getYaw(1.0F) != dir.asRotation());
                ClientEntrypoint.awaitTick();
            }

            public void moveForward(double amount) {
                FakePlayer player = FakePlayer.get();
                BlockPos target = Util.applyOffsetWithFacing(player.getHorizontalFacing(), player.getBlockPos(), new Vec3i(amount, 0, 0));
                player.setPositionTarget(target, 1);
                waitWhile(() -> !player.getNavigation().isIdle() || !player.isInWalkTargetRange());
            }
        }
    }
}
