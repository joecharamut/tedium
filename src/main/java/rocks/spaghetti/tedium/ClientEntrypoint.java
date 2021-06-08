package rocks.spaghetti.tedium;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Option;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.goals.GoalBlock;
import rocks.spaghetti.tedium.ai.path.AStarPathFinder;
import rocks.spaghetti.tedium.ai.path.Path;
import rocks.spaghetti.tedium.ai.path.PathContext;
import rocks.spaghetti.tedium.ai.path.PathExecutor;
import rocks.spaghetti.tedium.config.ModConfig;
import rocks.spaghetti.tedium.core.InteractionManager;
import rocks.spaghetti.tedium.events.*;
import rocks.spaghetti.tedium.render.DebugHud;
import rocks.spaghetti.tedium.render.RenderHelper;
import rocks.spaghetti.tedium.render.renderable.OutlineRegion;
import rocks.spaghetti.tedium.render.renderable.PathLine;
import rocks.spaghetti.tedium.render.renderable.Renderable;
import rocks.spaghetti.tedium.util.*;

import java.util.Optional;


public class ClientEntrypoint implements ClientModInitializer {
    private boolean connected = false;
    private PathExecutor executor = null;

    @Override
    public void onInitializeClient() {
        Log.info("Hello from {}!", Constants.MOD_ID);
        ModConfig.register(null, this::onSaveConfig);
        ModData.register();

        ClientTickEvents.END_CLIENT_TICK.register(KeyBindings::tick);
        ClientTickEvents.END_CLIENT_TICK.register(ScreenshotUtil::onClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!connected) return;

            InteractionManager.tick();

            while (KeyBindings.testKey.wasPressed()) {
                test();
            }
            if (executor != null) executor.tick();
        });

        HudRenderCallback.EVENT.register(DebugHud::render);

        WorldRenderEvents.START.register(RenderHelper::start);
        WorldRenderEvents.AFTER_ENTITIES.register(RenderHelper::afterEntities);
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(RenderHelper::beforeDebugRenderer);

        DeathCallback.EVENT.register(() -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;
            Log.info("Death: {}", player.getPos());
        });

        PauseMenuCallback.EVENT.register(() -> {
            Minecraft.setInputDisabled(false);
            executor = null;
        });

        KeyPressCallback.EVENT.register(key -> {
            if (Minecraft.isInputDisabled()) {
                for (KeyBinding bind : KeyBindings.modKeybindings) {
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

        ClientEvents.JOIN_WORLD.register(() -> {
            if (!connected) {
                connected = true;
                ModData.loadWorld();
            }
        });
        ClientEvents.DISCONNECT.register(() -> {
            if (connected) {
                connected = false;
                ModData.saveWorld();
            }
        });

        PlayerSprintCallback.EVENT.register(Minecraft::getForcedSprint);
    }

    private void test() {
//        BlockPos start = new BlockPos(184, 64, -110);
        BlockPos start = Minecraft.player().getBlockPos();
        BlockPos goal = new BlockPos(191, 64, -105);

        RenderHelper.clearListeners();
        Renderable startOutline = new OutlineRegion(start, 0xff0000, true);
        Renderable goalOutline = new OutlineRegion(goal, 0x00ff00, true);
        RenderHelper.addListener(() -> RenderHelper.queue(startOutline, goalOutline));

        AStarPathFinder pathFinder = new AStarPathFinder(start, new GoalBlock(goal), new PathContext(MinecraftClient.getInstance().world));

        new Thread(() -> {
            Optional<Path> result = pathFinder.calculate(-1);

            if (result.isPresent()) {
                Path path = result.get();
                Renderable pathLine = new PathLine(path.getPath(), 0xff00ff);
                RenderHelper.addListener(() -> RenderHelper.queue(pathLine));
                executor = new PathExecutor(path).onStop(() -> {
                    Minecraft.setInputDisabled(false);
                    executor = null;
                }).onSuccess(() -> {
                    Minecraft.sendMessage("Finished executing path");
                }).onError(() -> {
                    Minecraft.sendMessage("Error executing path");
                });
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
}
