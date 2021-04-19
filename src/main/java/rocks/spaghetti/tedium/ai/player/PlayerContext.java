package rocks.spaghetti.tedium.ai.player;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import rocks.spaghetti.tedium.util.Minecraft;
import rocks.spaghetti.tedium.util.Rotation;

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

    public Vec3d playerHead() {
        return player.getPos().add(0, player.getStandingEyeHeight(), 0);
    }

    public Rotation playerRotation() {
        return new Rotation(player.pitch, player.yaw);
    }
}
