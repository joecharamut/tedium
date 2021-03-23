package rocks.spaghetti.tedium.interaction.action;

import net.minecraft.client.MinecraftClient;

public class LookAction implements ClientAction {
    private final float yaw;

    public LookAction(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public boolean done() {
        return true;
    }

    @Override
    public void tick(MinecraftClient client) {
        client.player.yaw = yaw;
    }
}
