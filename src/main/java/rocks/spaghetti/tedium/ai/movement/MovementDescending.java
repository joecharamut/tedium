package rocks.spaghetti.tedium.ai.movement;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.path.PathContext;

import static rocks.spaghetti.tedium.ai.movement.ActionCosts.*;

public class MovementDescending extends Movement {

    public MovementDescending(PathContext context, BlockPos src, BlockPos dest) {
        super(src, dest);
    }

    public static double cost(PathContext context, BlockPos src, BlockPos dest) {
        if (!context.canWalkThrough(dest)) {
            // todo break blocks?
            return INFINITY;
        }

        if (!context.canWalkOn(dest.down())) {
            // todo fall
            return INFINITY;
        }

        double walkCost = WALK_OFF_BLOCK_COST;
        if (context.get(src.down()).getBlock() == Blocks.SOUL_SAND) {
            walkCost *= WALK_ON_SOUL_SAND_COST / WALK_COST;
        }
        return walkCost + Math.max(FALL_N_BLOCKS_COST[1], CENTER_AFTER_FALLING_COST);
    }

    @Override
    public double calculateCost(PathContext context) {
        return cost(context, src, dest);
    }
}
