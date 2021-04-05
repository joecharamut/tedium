package rocks.spaghetti.tedium;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.options.Option;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotUtils;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import rocks.spaghetti.tedium.config.ModConfig;
import rocks.spaghetti.tedium.core.AbstractInventory;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.core.InteractionManager;
import rocks.spaghetti.tedium.crafting.Recipes;
import rocks.spaghetti.tedium.mixin.MinecraftClientMixin;
import rocks.spaghetti.tedium.render.ControlGui;
import rocks.spaghetti.tedium.render.DebugHud;
import rocks.spaghetti.tedium.render.RenderHelper;
import rocks.spaghetti.tedium.web.WebHandlers;
import rocks.spaghetti.tedium.web.WebServer;

import java.net.BindException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static net.minecraft.util.Util.NIL_UUID;


public class ClientEntrypoint implements ClientModInitializer {
    private static final ExecutorQueue runInClientThread = new ExecutorQueue();
    private static boolean disableInput = false;
    private static boolean debugEnabled = false;
    private static AbstractInventory currentContainer = null;

    private final WebServer webServer = new WebServer();
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

    public static final KeyBinding[] modKeybindings = {
            openMenuKey, toggleDebugKey
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

    public static void sendClientMessage(MutableText message) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;
        if (message.getStyle().getColor() == null) {
            message = message.formatted(Formatting.WHITE);
        }

        MutableText text = new LiteralText("[Tedium] ").formatted(Formatting.YELLOW).append(message);
        client.player.sendSystemMessage(text, NIL_UUID);
    }

    public static void sendClientMessage(String message) {
        sendClientMessage(new LiteralText(message).formatted(Formatting.WHITE));
    }

    public static boolean isInputDisabled() {
        return disableInput;
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void onGameMenuOpened() {
        setFakePlayerState(false);
    }

    public static void setOpenContainer(AbstractInventory newContainer) {
        currentContainer = newContainer;
    }

    public static AbstractInventory getOpenContainer() {
        return currentContainer;
    }

    public static void setFakePlayerState(boolean enabled) {
        FakePlayer fake = FakePlayer.get();
        MinecraftClient client = MinecraftClient.getInstance();

        if (enabled && fake.isAiDisabled()) {
            disableInput = true;
            client.mouse.unlockCursor();
            sendClientMessage(new TranslatableText("text.tedium.aiEnabled"));
            fake.setAiDisabled(false);
        } else if (!enabled && !fake.isAiDisabled()) {
            disableInput = false;
            fake.setAiDisabled(true);
            sendClientMessage(new TranslatableText("text.tedium.aiDisabled"));
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
        RenderHelper.addListener(() -> {
            RenderHelper.queueRenderable(new RenderHelper.OutlineRegion(new BlockPos(207, 65, -120), 0x00ff00));

            int x1 = 211;
            int x2 = 211;
            int y1 = 66;
            int y2 = 70;
            int z1 = -125;
            int z2 = -119;

            for (int a = x1; a <= x2; a++) {
                for (int b = y1; b <= y2; b++) {
                    for (int c = z1; c <= z2; c++) {
                        RenderHelper.queueRenderable(new RenderHelper.FloatingText(new LiteralText("☺"), new BlockPos(a, b, c)));
                    }
                }
            }
        });
    }

    private void onSaveConfig(ModConfig config) {
        if (ModConfig.isFullbrightEnabled()) {
            Option.GAMMA.setMax(100.0f);
        } else {
            Option.GAMMA.setMax(1.0f);
        }
    }

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

        while (openMenuKey.wasPressed()) {
            setFakePlayerState(false);
            client.openScreen(ControlGui.createScreen());
        }

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

        if (ModConfig.isWebServerEnabled() && !webServer.isRunning()) {
            try {
                webServer.startServer();
                new WebHandlers().registerWebContexts(webServer);
                String webAddress = "http://localhost:" + ModConfig.getWebServerPort();
                sendClientMessage(new TranslatableText("text.tedium.webServerStarted")
                        .formatted(Formatting.WHITE)
                        .append(new LiteralText(webAddress)
                                .formatted(Formatting.WHITE).formatted(Formatting.UNDERLINE)
                                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, webAddress)))));
            } catch (BindException ex) {
                sendClientMessage(new TranslatableText("text.tedium.webErrorStarting").formatted(Formatting.RED));
            }
        }
    }

    private void destroyComponents() {
        ModData.saveWorld();

        if (webServer.isRunning()) {
            webServer.stopServer();
        }
    }
}
