package rocks.spaghetti.tedium.ai.movement;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.path.PathContext;

public class MovementAscending extends Movement {
    public MovementAscending(PathContext context, BlockPos src, BlockPos dest) {
        super(src, dest);
    }

    public static double cost(PathContext context, BlockPos src, BlockPos dest) {
        if (!context.canWalkOn(dest.down())) {
            return ActionCosts.INFINITY;
        }

        if (
                context.get(src.up(3)).getBlock() instanceof FallingBlock
                && ( context.canWalkThrough(src.up()) || !(context.get(src.up(2)).getBlock() instanceof FallingBlock) )
        ) {
            return ActionCosts.INFINITY;
        }

        Block srcDown = context.get(src.down()).getBlock();
        if (srcDown == Blocks.LADDER || srcDown == Blocks.VINE) {
            return ActionCosts.INFINITY;
        }

        if (!context.canWalkThrough(src.up(2)) || !context.canWalkThrough(dest) || !context.canWalkThrough(dest.up())) {
            return ActionCosts.INFINITY;
        }

        return ActionCosts.WALK_COST + ActionCosts.JUMP_COST;
    }

    @Override
    public double calculateCost(PathContext context) {
        return cost(context, src, dest);
    }
}
