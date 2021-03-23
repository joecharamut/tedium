package rocks.spaghetti.tedium.core;

import net.minecraft.client.MinecraftClient;
import rocks.spaghetti.tedium.interaction.ClientInteractionHelper;

public class PlayerCore {
    public final WorldView worldView = new WorldView();
    public final ClientInteractionHelper interactionHelper = new ClientInteractionHelper();

    public PlayerCore() {

    }

    public void tick(MinecraftClient client) {
        worldView.update();
        interactionHelper.tick(client);
    }
}
