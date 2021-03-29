package rocks.spaghetti.tedium.script;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.*;
import rocks.spaghetti.tedium.ClientEntrypoint;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.Util;
import rocks.spaghetti.tedium.core.AbstractInventory;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.core.InteractionManager;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

public class ScriptEnvironment {
    private final ScriptExecutor scriptExecutor = new ScriptExecutor();

    public void eval(String code) {
        try (Scope scope = new Scope()) {
            Log.info("eval> {}", code);
            Object result = scope.execScript(scope.compile(code, "<eval>"));
            Log.info("eval> {}", scope.toString(result));
        } catch (RhinoException e) {
            Log.error("Script Error: {}", e.getMessage());
        } catch (Exception e) {
            Log.error("Unhandled Exception: {}", e.getClass());
            Log.catching(e);
        }
    }

    public void execResource(String resourceLocation) {
        scriptExecutor.execute(() -> {
            try (Scope scope = new Scope()) {
                String code = Util.getResourceAsString(resourceLocation);
                if (code.isEmpty()) {
                    Log.warn("Failed to load resource!");
                    return;
                }
                Script script = scope.compile(code, resourceLocation);
                scope.execScript(script);
            } catch (RhinoException e) {
                Log.error("Script Error: {}", e.getMessage());
            } catch (Exception e) {
                Log.error("Unhandled Exception: {}", e.getClass());
                Log.catching(e);
            }
        });

        if (!scriptExecutor.isAlive()) scriptExecutor.start();
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
            while (!Thread.interrupted()) {
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
        private final Context context;
        private final Scriptable scriptScope;

        public Scope() {
            context = Context.enter();
            scriptScope = context.initStandardObjects();
            Object sysWrapper = Context.javaToJS(new SysMethods(), scriptScope);
            ScriptableObject.putProperty(scriptScope, "sys", sysWrapper);
            Object apiWrapper = Context.javaToJS(new MinecraftApi(), scriptScope);
            ScriptableObject.putProperty(scriptScope, "minecraft", apiWrapper);
        }

        public Script compile(String source, String sourceName) throws IOException {
            return compile(new StringReader(source), sourceName);
        }

        public Script compile(Reader reader, String sourceName) throws IOException {
            return context.compileReader(reader, sourceName, 0, null);
        }

        public Object execScript(Script script) {
            return script.exec(context, scriptScope);
        }

        @Override
        public void close() {
            Context.exit();
        }

        public String toString(Object result) {
            return Context.toString(result);
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class SysMethods {
            public void println(String msg) {
                Log.info("[Script] {}", msg);
            }
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class MinecraftApi {
            public void goToBlock(int x, int y, int z) throws InterruptedException {
                FakePlayer player = FakePlayer.get();
                if (player == null) return;

                BlockPos dest = new BlockPos(x, y, z);
                int range = 1;

                player.setPositionTarget(dest, range);

                while (player.squaredDistanceTo(Util.Vec3iToVec3d(dest)) > range+2) { Thread.sleep(100); }
            }

            public List<ItemStack> openContainerAt(int x, int y, int z) throws InterruptedException {
                FakePlayer player = FakePlayer.get();
                if (player == null) return Collections.emptyList();

                BlockPos blockPos = new BlockPos(x, y, z);
                Block block = player.world.getBlockState(blockPos).getBlock();
                if (!block.is(Blocks.CHEST)) return Collections.emptyList();


                InteractionManager.pushEvent(new InteractionManager.BlockInteractEvent(blockPos));
                while (ClientEntrypoint.getOpenContainer() == null) { Thread.sleep(100); }

                return ClientEntrypoint.getOpenContainer().getExternalStacks();
            }

            public void closeContainer() {
                FakePlayer player = FakePlayer.get();
                if (player == null) return;
                AbstractInventory container = ClientEntrypoint.getOpenContainer();
                if (container == null) return;
                container.close();
            }
        }
    }
}
