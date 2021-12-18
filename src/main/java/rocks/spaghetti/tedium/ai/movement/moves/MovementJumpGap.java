package rocks.spaghetti.tedium.ai.movement.moves;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import rocks.spaghetti.tedium.ai.movement.ActionCosts;
import rocks.spaghetti.tedium.ai.movement.Input;
import rocks.spaghetti.tedium.ai.movement.MovementState;
import rocks.spaghetti.tedium.ai.path.PathContext;
import rocks.spaghetti.tedium.ai.player.PlayerContext;
import rocks.spaghetti.tedium.util.RotationUtil;

public class MovementJumpGap extends MovementHorizontal {
    public MovementJumpGap(PathContext context, BlockPos src, BlockPos dest) {
        super(context, src, dest);
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

    @Override
    public MovementState updateState(PlayerContext context, MovementState state) {
        state = super.updateState(context, state);

        state.setInput(Input.JUMP, true);
        state.setLookTarget(RotationUtil.rotationFromPosition(context.playerHead(), Vec3d.ofCenter(dest.up()), context.playerRotation()));

        return state;
    }
}
