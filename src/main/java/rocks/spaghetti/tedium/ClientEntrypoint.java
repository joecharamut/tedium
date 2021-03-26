package rocks.spaghetti.tedium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
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
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import rocks.spaghetti.tedium.config.ModConfig;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.core.InteractionManager;
import rocks.spaghetti.tedium.core.PlayerCore;
import rocks.spaghetti.tedium.crafting.Recipes;
import rocks.spaghetti.tedium.hud.DebugHud;
import rocks.spaghetti.tedium.mixin.MinecraftClientMixin;
import rocks.spaghetti.tedium.web.WebServer;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class ClientEntrypoint implements ClientModInitializer {
    private static final ExecutorQueue runInClientThread = new ExecutorQueue();
    private static boolean fakePlayerState = false;
    private static boolean disableInput = false;

    private final WebServer webServer = new WebServer();
    private final DebugHud debugHud = new DebugHud();
    private PlayerCore playerCore = null;
    private boolean connected = false;
    private boolean debugEnabled = false;

    private static final KeyBinding toggleAiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tedium.toggleAi",
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
            toggleAiKey, toggleDebugKey
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
        client.player.sendSystemMessage(text, Util.NIL_UUID);
    }

    public static void sendClientMessage(String message) {
        sendClientMessage(new LiteralText(message).formatted(Formatting.WHITE));
    }

    public static boolean isInputDisabled() {
        return disableInput;
    }

    public static void onGameMenuOpened() {
        setFakePlayerState(false);
    }

    private static void toggleFakePlayerState() {
        setFakePlayerState(!fakePlayerState);
    }

    private static void setFakePlayerState(boolean enabled) {
        fakePlayerState = enabled;

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

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ModData.saveGlobal();
        });
    }

    private void onSaveConfig(ModConfig config) {
        if (ModConfig.isFullbrightEnabled()) {
            Option.GAMMA.setMax(100.0f);
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

        playerCore.tick(client);
        InteractionManager.tick();

        if (toggleAiKey.wasPressed()) {
            toggleFakePlayerState();
        }

        if (testKey.wasPressed()) {
            sendClientMessage("hi!");
        }

        if (toggleDebugKey.wasPressed()) {
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
        playerCore = new PlayerCore();

        if (ModConfig.isWebServerEnabled() && !webServer.isRunning()) {
            try {
                webServer.startServer();
                registerWebContexts();
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
        playerCore = null;

        if (webServer.isRunning()) {
            webServer.stopServer();
        }
    }

    private void registerWebContexts() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        assert client.world != null;

        webServer.registerPath("/screenshot", session -> {
            byte[] imageBytes = { };
            try {
                imageBytes = takeScreenshotBlocking().getBytes();
            } catch (IOException e) { Log.catching(e); }

            return WebServer.bytesResponse(imageBytes, "image/png");
        });


        webServer.registerPath("/name", (WebServer.StringRequestHandler) () -> client.player.getDisplayName().asString());
        webServer.registerPath("/position", (WebServer.StringRequestHandler) () -> client.player.getPos().toString());
        webServer.registerPath("/under", (WebServer.StringRequestHandler) () -> client.world.getBlockState(client.player.getBlockPos().down()).toString());

        webServer.registerPath("/look", session -> {
            Map<String, List<String>> query = session.getParameters();

            float yaw = 0;
            if (query.containsKey("yaw")) {
                yaw = Float.parseFloat(query.get("yaw").get(0));
            }

            client.player.yaw = yaw;
            return WebServer.stringResponse("OK");
        });

        webServer.registerPath("/near", (WebServer.StringRequestHandler) () -> {
            playerCore.worldView.update();
            return playerCore.worldView.blocks.toString();
        });
    }
}
