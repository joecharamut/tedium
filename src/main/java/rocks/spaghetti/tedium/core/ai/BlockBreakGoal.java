package rocks.spaghetti.tedium.core.ai;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.core.PlayerInventoryHelper;

import java.util.EnumSet;

public class BlockBreakGoal extends Goal {
    private final FakePlayer player;
    private BlockPos targetBlock = null;

    public BlockBreakGoal(FakePlayer player) {
        this.player = player;
        this.setControls(EnumSet.allOf(Goal.Control.class));
    }

    public void setTarget(BlockPos target) {
        targetBlock = target;
    }

    @Override
    public boolean canStart() {
        return targetBlock != null
                && !player.world.getBlockState(targetBlock).isAir()
                && player.world.canPlayerModifyAt(player.getRealPlayer(), targetBlock)
                && (!player.world.getBlockState(targetBlock).isToolRequired()
                        || PlayerInventoryHelper.getToolSlotFor(player, player.world.getBlockState(targetBlock)) != -1);
    }
}
