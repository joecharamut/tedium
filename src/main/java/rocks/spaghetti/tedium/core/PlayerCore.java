package rocks.spaghetti.tedium.core;

import net.minecraft.client.MinecraftClient;

public class PlayerCore {
    public final WorldView worldView = new WorldView();

    public PlayerCore() {

    }

    public void tick(MinecraftClient client) {
        worldView.update();
    }
}
