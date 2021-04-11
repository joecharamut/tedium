package rocks.spaghetti.tedium.ai.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class PlayerControls {
    private final ClientPlayerEntity player;

    private boolean enabled = false;

    protected PlayerControls(ClientPlayerEntity player) {
        this.player = player;
    }

    private void assertEnabled() {
        if (!enabled) throw new IllegalStateException("Controls Not Enabled");
    }

    public void setEnabled(boolean state) {
        enabled = state;
        if (state) {
            player.input = new KeyboardInputBlocker();
            MinecraftClient.getInstance().mouse.unlockCursor();
        } else {
            player.input = new KeyboardInput(MinecraftClient.getInstance().options);
        }
    }

    public void jumping(boolean state) {
        assertEnabled();
        player.input.jumping = state;
    }

    public void sneaking(boolean state) {
        assertEnabled();
        player.input.sneaking = state;
    }

    public void sprinting(boolean state) {
        assertEnabled();
        player.setSprinting(state);
    }

    public void movement(float x, float z) {
        assertEnabled();
        player.input.movementForward = MathHelper.clamp(x, -1.0F, 1.0F);
        player.input.movementSideways = MathHelper.clamp(z, -1.0F, 1.0F);
    }

    public void look(float pitch, float yaw) {
        assertEnabled();
        player.pitch = pitch;
        player.yaw = yaw;
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
