package rocks.spaghetti.tedium.ai.player;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import rocks.spaghetti.tedium.util.Minecraft;

import java.util.Optional;

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
        Minecraft.setInputDisabled(state);
    }

    public void clear() {
        jumping(false);
        sneaking(false);
        sprinting(false);
        movement(0, 0);
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
        Minecraft.setForcedSprint(TriState.of(state));
        player.setSprinting(state);
    }

    public void movement(float x, float z) {
        assertEnabled();
        player.input.movementForward = MathHelper.clamp(x, -1.0F, 1.0F);
        player.input.movementSideways = MathHelper.clamp(z, -1.0F, 1.0F);
    }

    public void look(float pitch, float yaw) {
        assertEnabled();
        player.setPitch(pitch);
        player.setYaw(yaw);
    }
}
