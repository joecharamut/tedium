package rocks.spaghetti.tedium.ai.player;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.util.Minecraft;

public class PlayerContext {
    private final ClientPlayerEntity player;
    private final PlayerControls controls;
    private final PlayerInventory inventory;

    public PlayerContext() {
        player = Minecraft.player();
        controls = new PlayerControls(player);
        controls.setEnabled(true);
        inventory = new PlayerInventory(player);
    }

    public ClientPlayerEntity player() {
        return player;
    }

    public PlayerControls controls() {
        return controls;
    }

    public BlockPos pos() {
        return player.getBlockPos();
    }

    public PlayerInventory inventory() {
        return inventory;
    }
}
