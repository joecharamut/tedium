package rocks.spaghetti.tedium;

import net.fabricmc.api.ClientModInitializer;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import rocks.spaghetti.tedium.config.ModConfig;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.core.PlayerCore;
import rocks.spaghetti.tedium.hud.DebugHud;
import rocks.spaghetti.tedium.mixin.MinecraftClientMixin;
import rocks.spaghetti.tedium.web.WebServer;

import java.io.IOException;
import java.net.BindException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public class ClientEntrypoint implements ClientModInitializer {
    public static boolean disableMouseInput = false;

    private PlayerCore playerCore = null;
    private final WebServer webServer = new WebServer();
    private final DebugHud debugHud = new DebugHud();
    private final ExecutorQueue runInClientThread = new ExecutorQueue();
    private boolean connected = false;

    private static final KeyBinding openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tedium.openMenu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "category.tedium.keys"
    ));

    private static final KeyBinding testKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tedium.test",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "category.tedium.keys"
    ));

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

    private void test() {
        FakePlayer fake = FakePlayer.get();
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (fake.isAiDisabled()) {
            disableMouseInput = true;
            client.mouse.unlockCursor();
            player.sendMessage(new LiteralText("Beginning fake player test..."), false);

            fake.initGoals();
            fake.initBrain();
            fake.setAiDisabled(false);
        } else {
            disableMouseInput = false;
            fake.setAiDisabled(true);
            player.sendMessage(new LiteralText("Stopped"), false);
        }
    }

    @Override
    public void onInitializeClient() {
        Log.info("Hello from {}!", Constants.MOD_ID);
        ModConfig.register(null, this::onSaveConfig);

        ClientTickEvents.END_CLIENT_TICK.register(this::endClientTick);

        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            GameOptions options = ((MinecraftClientMixin) MinecraftClient.getInstance()).getGameOptions();

            // dont draw over/under debug menu or player list
            if (options.debugEnabled) return;
            if (!client.isInSingleplayer() && options.keyPlayerList.isPressed()) return;
            debugHud.render(matrixStack, tickDelta);
        });
    }

    private void onSaveConfig(ModConfig config) {
        if (ModConfig.isFullbrightEnabled()) {
            Option.GAMMA.setMax(100);
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

        while (openMenu.wasPressed()) {
            test();
        }

        while (testKey.wasPressed()) {
            BlockPos pos = client.player.getBlockPos().add(0, 3, 0);
            client.interactionManager.interactBlock(
                    client.player,
                    client.world,
                    Hand.MAIN_HAND,
                    new BlockHitResult(Vec3d.ofBottomCenter(pos), Direction.NORTH, pos, false));
        }

        runInClientThread.runNext();
    }

    private void initializeComponents() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.world != null;
        assert client.player != null;

        // initialize everything for real
        Recipes.initRecipes(client.world);
        playerCore = new PlayerCore();

        if (ModConfig.isWebServerEnabled() && !webServer.isRunning()) {
            try {
                webServer.startServer();
                registerWebContexts();
                MutableText text = new LiteralText("[Tedium] ").formatted(Formatting.YELLOW)
                        .append(new TranslatableText("text.tedium.webServerStarted").formatted(Formatting.WHITE))
                        .append(new LiteralText("http://localhost:" + ModConfig.getWebServerPort())
                                .formatted(Formatting.WHITE).formatted(Formatting.UNDERLINE)
                                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://localhost:" + ModConfig.getWebServerPort()))));
                client.player.sendSystemMessage(text, Util.NIL_UUID);
            } catch (BindException ex) {
                MutableText text = new LiteralText("[Tedium] ").formatted(Formatting.YELLOW)
                        .append(new TranslatableText("text.tedium.webErrorStarting").formatted(Formatting.RED));
                client.player.sendSystemMessage(text, Util.NIL_UUID);
            }
        }
    }

    private void destroyComponents() {
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
            final CountDownLatch latch = new CountDownLatch(1);
            final NativeImage[] image = { null };

            runInClientThread.execute(() -> {
                Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
                image[0] = ScreenshotUtils.takeScreenshot(framebuffer.textureWidth, framebuffer.textureHeight, framebuffer);
                latch.countDown();
            });

            try {
                latch.await();
            } catch (InterruptedException e) { Log.catching(e); }

            byte[] imageBytes = { };
            try {
                imageBytes = image[0].getBytes();
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
