package rocks.spaghetti.tedium.script;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.jetbrains.annotations.NotNull;
import rocks.spaghetti.tedium.ClientEntrypoint;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.Util;
import rocks.spaghetti.tedium.core.AbstractInventory;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.core.InteractionManager;

import javax.script.*;
import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

public class ScriptEnvironment {
    private final ScriptExecutor scriptExecutor = new ScriptExecutor();

    public void execResource(String resourceLocation) {
        scriptExecutor.execute(() -> {
            try (Scope scope = new Scope()) {
                String code = Util.getResourceAsString(resourceLocation);
                if (code.isEmpty()) {
                    Log.warn("Failed to load resource!");
                    return;
                }
                scope.eval(code);
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
        private final GraalJSScriptEngine engine;

        public Scope() {
            engine = GraalJSScriptEngine.create(null,
                    Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)
                    .allowHostClassLookup(s -> true)
                    .option("js.ecmascript-version", "2021")
            );
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("sys", new SysMethods());
            bindings.put("minecraft", new MinecraftApi());
        }

        public Object eval(String source) throws ScriptException {
            return engine.eval(source, engine.getContext());
        }

        @Override
        public void close() {
            engine.close();
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class SysMethods {
            public void log(Object msg) {
                Log.info("[Script] {}", msg);
            }
        }

        @SuppressWarnings({"unused", "RedundantSuppression"})
        public static class MinecraftApi {
            public void sendMessage(String message) {
                ClientEntrypoint.sendClientMessage(message);
            }

            public boolean aiEnabled() {
                FakePlayer player = FakePlayer.get();
                if (player == null) return false;
                return !player.isAiDisabled();
            }

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

            public boolean quickMoveStack(int slot) {
                FakePlayer player = FakePlayer.get();
                if (player == null) return false;
                AbstractInventory container = ClientEntrypoint.getOpenContainer();
                if (container == null) return false;
                container.quickMoveStack(slot);
                return true;
            }

            public void closeContainer() {
                FakePlayer player = FakePlayer.get();
                if (player == null) return;
                AbstractInventory container = ClientEntrypoint.getOpenContainer();
                if (container == null) return;
                container.close();
            }

            public BlockPos getPos() {
                FakePlayer player = FakePlayer.get();
                if (player == null) return null;
                return player.getBlockPos();
            }

            public BlockState getBlockStateAt(int x, int y, int z) {
                FakePlayer player = FakePlayer.get();
                if (player == null) return null;

                return player.world.getBlockState(new BlockPos(x, y, z));
            }
        }
    }
}
