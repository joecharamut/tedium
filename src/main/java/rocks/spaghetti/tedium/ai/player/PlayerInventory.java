package rocks.spaghetti.tedium.ai.player;

import net.minecraft.client.network.ClientPlayerEntity;

public class PlayerInventory {
    private final ClientPlayerEntity player;

    protected PlayerInventory(ClientPlayerEntity player) {
        this.player = player;
    }

}
