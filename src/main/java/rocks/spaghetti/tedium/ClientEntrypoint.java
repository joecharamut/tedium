package rocks.spaghetti.tedium;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.options.Option;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotUtils;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import rocks.spaghetti.tedium.ai.goals.GoalBlock;
import rocks.spaghetti.tedium.ai.path.*;
import rocks.spaghetti.tedium.config.ModConfig;
import rocks.spaghetti.tedium.events.DeathCallback;
import rocks.spaghetti.tedium.events.KeyPressCallback;
import rocks.spaghetti.tedium.events.MouseEvents;
import rocks.spaghetti.tedium.events.PauseMenuCallback;
import rocks.spaghetti.tedium.util.*;
import rocks.spaghetti.tedium.core.InteractionManager;
import rocks.spaghetti.tedium.crafting.Recipes;
import rocks.spaghetti.tedium.mixin.MinecraftClientMixin;
import rocks.spaghetti.tedium.render.ControlGui;
import rocks.spaghetti.tedium.render.DebugHud;
import rocks.spaghetti.tedium.render.RenderHelper;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;


public class ClientEntrypoint implements ClientModInitializer {
    private static final ExecutorQueue runInClientThread = new ExecutorQueue();
    private static final Latch tickLatch = new Latch();
    private static boolean debugEnabled = false;

    private final DebugHud debugHud = new DebugHud();
    private boolean connected = false;

    private static final KeyBinding openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tedium.openMenu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            Constants.CATEGORY_KEYS
    ));

    private static final KeyBinding toggleDebugKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tedium.toggleDebug",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            Constants.CATEGORY_KEYS
    ));

    private static final KeyBinding testKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tedium.test",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            Constants.CATEGORY_KEYS
    ));

    public static final KeyBinding[] modKeybindings = {
            openMenuKey, toggleDebugKey, testKey
    };

    private static class ExecutorQueue implements Executor {
        final Queue<Runnable> tasks = new ArrayDeque<>();
        Runnable active = null;

        @Override
        public synchronized void execute(@NotNull final Runnable runnable) {
            tasks.offer(runnable);
        }

        protected synchronized void runNext() {
            if ((active = tasks.poll()) != null) {
                active.run();
            }
        }
    }

    public static void takeScreenshot(Consumer<NativeImage> callback) {
        runInClientThread.execute(() -> {
            Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
            callback.accept(ScreenshotUtils.takeScreenshot(framebuffer.textureWidth, framebuffer.textureHeight, framebuffer));
        });
    }

    public static NativeImage takeScreenshotBlocking() {
        final CountDownLatch latch = new CountDownLatch(1);
        final NativeImage[] imageHolder = { null };

        takeScreenshot(image -> {
            imageHolder[0] = image;
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.catching(e);
            Thread.currentThread().interrupt();
        }

        return imageHolder[0];
    }

    public static void awaitTick() {
        try {
            tickLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onInitializeClient() {
        Log.info("Hello from {}!", Constants.MOD_ID);
        ModConfig.register(null, this::onSaveConfig);
        ModData.register();

        ClientTickEvents.END_CLIENT_TICK.register(this::endClientTick);

        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            GameOptions options = ((MinecraftClientMixin) MinecraftClient.getInstance()).getGameOptions();

            // dont draw over/under debug menu or player list
            if (options.debugEnabled) return;
            if (!client.isInSingleplayer() && options.keyPlayerList.isPressed()) return;

            if (debugEnabled) {
                debugHud.render(matrixStack, tickDelta);
            }
        });

        WorldRenderEvents.START.register(RenderHelper::start);
        WorldRenderEvents.AFTER_ENTITIES.register(RenderHelper::afterEntities);
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(RenderHelper::beforeDebugRenderer);

        DeathCallback.EVENT.register(() -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;
            Log.info("Death: {}", player.getPos());
        });

        PauseMenuCallback.EVENT.register(() -> Minecraft.setInputDisabled(false));

        KeyPressCallback.EVENT.register(key -> {
            if (Minecraft.isInputDisabled()) {
                for (KeyBinding bind : ClientEntrypoint.modKeybindings) {
                    if (key.getTranslationKey().equals(bind.getBoundKeyTranslationKey())) {
                        return ActionResult.PASS;
                    }
                }

                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        MouseEvents.LOCK_EVENT.register(() -> {
            if (Minecraft.isInputDisabled()) return ActionResult.FAIL;
            return ActionResult.PASS;
        });
        MouseEvents.BUTTON_EVENT.register((window, button, action, mods) -> {
            if (Minecraft.isInputDisabled()) return ActionResult.FAIL;
            return ActionResult.PASS;
        });
        MouseEvents.SCROLL_EVENT.register((window, horizontal, vertical) -> {
            if (Minecraft.isInputDisabled()) return ActionResult.FAIL;
            return ActionResult.PASS;
        });
    }

    private void test() {
//        BlockPos start = new BlockPos(184, 64, -110);
        BlockPos start = MinecraftClient.getInstance().player.getBlockPos();
        BlockPos goal = new BlockPos(191, 64, -105);

        RenderHelper.clearListeners();
        RenderHelper.Renderable startOutline = new RenderHelper.OutlineRegion(start, Color.RED.getRGB(), true);
        RenderHelper.Renderable goalOutline = new RenderHelper.OutlineRegion(goal, Color.GREEN.getRGB(), true);
        RenderHelper.addListener(() -> RenderHelper.queue(startOutline, goalOutline));

        AStarPathFinder pathFinder = new AStarPathFinder(start, new GoalBlock(goal), new PathContext(MinecraftClient.getInstance().world));

        new Thread(() -> {
            Optional<Path> result = pathFinder.calculate(-1);

            if (result.isPresent()) {
                Path path = result.get();
                RenderHelper.Renderable pathLine = new RenderHelper.PathLine(path.getPath(), 0xff00ff);
                RenderHelper.addListener(() -> RenderHelper.queue(pathLine));
                executor = new PathExecutor(path);
            }
        }).start();
    }

    private void onSaveConfig(ModConfig config) {
        if (ModConfig.isFullbrightEnabled()) {
            Option.GAMMA.setMax(100.0f);
        } else {
            Option.GAMMA.setMax(1.0f);
        }
    }

    private PathExecutor executor = null;
    private void endClientTick(MinecraftClient client) {
        if (connected && client.player == null && client.world == null) {
            // client disconnected from world
            destroyComponents();
            connected = false;
            return;
        }

        if (client.player == null) return;
        if (client.world == null) return;

        if (!connected) {
            // client joined new world
            initializeComponents();
            connected = true;
        }

        InteractionManager.tick();
        tickLatch.release();

        while (openMenuKey.wasPressed()) {
            client.openScreen(ControlGui.createScreen());
        }

        while (testKey.wasPressed()) {
            test();
        }
        if (executor != null) executor.tick();

        while (toggleDebugKey.wasPressed()) {
            debugEnabled = !debugEnabled;
        }

        runInClientThread.runNext();
    }

    private void initializeComponents() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.world != null;
        assert client.player != null;

        // initialize everything for real
        ModData.loadWorld();
        Recipes.initRecipes(client.world);
    }

    private void destroyComponents() {
        ModData.saveWorld();
    }
}
