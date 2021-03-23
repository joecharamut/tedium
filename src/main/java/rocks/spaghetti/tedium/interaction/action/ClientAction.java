package rocks.spaghetti.tedium.interaction.action;

import net.minecraft.client.MinecraftClient;

public interface ClientAction {
    boolean done();
    void tick(MinecraftClient client);
}
