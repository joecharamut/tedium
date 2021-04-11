package rocks.spaghetti.tedium.ai.movement;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.path.PathContext;

public class MovementHorizontal extends Movement {
    public MovementHorizontal(PathContext context, BlockPos src, BlockPos dest) {
        super(src, dest);
    }

    @Override
    public double calculateCost(PathContext context) {
        return cost(context, src, dest);
    }

    public static double cost(PathContext context, BlockPos src, BlockPos dest) {
        BlockState destHead = context.get(dest.up());
        BlockState destFeet = context.get(dest);
        BlockState destFloor = context.get(dest.down());

        if (!context.canWalkThrough(destFeet, dest) || !context.canWalkThrough(destHead, dest.up())) {
            // todo mining
            return ActionCosts.INFINITY;
        }

        if (context.canWalkOn(destFloor, dest.down())) {
            return ActionCosts.WALK_COST;
        }

        return ActionCosts.INFINITY;
    }
}
