package rocks.spaghetti.tedium.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.GameOptions;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import static net.minecraft.util.Util.NIL_UUID;

public class Minecraft {
    private Minecraft() { throw new IllegalStateException("Utility Class"); }

    private static GenericContainer openContainer = null;
    public static void setOpenContainer(GenericContainer container) {
        openContainer = container;
    }
    public static GenericContainer getOpenContainer() {
        return openContainer;
    }

    private static boolean inputDisabled = false;
    public static void setInputDisabled(boolean state) {
        inputDisabled = state;
        if (state) {
            player().input = new KeyboardInputBlocker();
            mouse().unlockCursor();
        } else {
            player().input = new KeyboardInput(Minecraft.options());
        }
    }
    public static boolean isInputDisabled() {
        return inputDisabled;
    }

    public static Mouse mouse() {
        return MinecraftClient.getInstance().mouse;
    }

    public static GameOptions options() {
        return MinecraftClient.getInstance().options;
    }

    public static void sendMessage(MutableText message) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;
        if (message.getStyle().getColor() == null) {
            message = message.formatted(Formatting.WHITE);
        }

        MutableText text = new LiteralText("[Tedium] ").formatted(Formatting.YELLOW).append(message);
        client.player.sendSystemMessage(text, NIL_UUID);
    }

    public static void sendMessage(String message) {
        sendMessage(new LiteralText(message).formatted(Formatting.WHITE));
    }

    public static ClientPlayerEntity player() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) throw new IllegalStateException("Player is Null");
        return player;
    }

    private static class KeyboardInputBlocker extends KeyboardInput {
        public KeyboardInputBlocker() {
            super(null);
        }

        @Override
        public void tick(boolean slowDown) {
            // just disable the player inputs nothing special here
        }
    }
}
