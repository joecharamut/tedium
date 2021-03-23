package rocks.spaghetti.tedium.interaction.action;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import rocks.spaghetti.tedium.Util;

public class BlockBreakAction implements ClientAction {
    private Vec3i offset = null;
    private BlockPos toBreak = null;
    private boolean breaking = false;

    public BlockBreakAction(BlockPos toBreak) {
        this.toBreak = toBreak;
    }

    public BlockBreakAction(int offX, int offY, int offZ) {
        this.offset = new Vec3i(offX, offY, offZ);
    }

    @Override
    public boolean done() {
        return !breaking;
    }

    @Override
    public void tick(MinecraftClient client) {
        if (offset != null) {
            toBreak = Util.applyOffsetWithFacing(client.player.getHorizontalFacing(), client.player.getBlockPos(), offset);
            offset = null;
        }

        breaking = client.interactionManager.updateBlockBreakingProgress(toBreak, Direction.DOWN);
    }
}
