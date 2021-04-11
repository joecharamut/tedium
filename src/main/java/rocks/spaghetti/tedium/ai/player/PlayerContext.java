package rocks.spaghetti.tedium.ai.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class PlayerContext {
    private final ClientPlayerEntity player;
    private final PlayerControls controls;

    public PlayerContext() {
        player = MinecraftClient.getInstance().player;
        if (player == null) throw new IllegalStateException("Null Player");

        controls = new PlayerControls(player);
    }

    public PlayerControls controls() {
        return controls;
    }

    public BlockPos pos() {
        return player.getBlockPos();
    }
}
