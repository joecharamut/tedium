package rocks.spaghetti.tedium.ai.movement;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.path.Path;
import rocks.spaghetti.tedium.ai.path.PathContext;
import rocks.spaghetti.tedium.ai.player.PlayerContext;

public class MovementHorizontal extends Movement {
    private final PathContext pathContext;
    public MovementHorizontal(PathContext context, BlockPos src, BlockPos dest) {
        super(src, dest);
        this.pathContext = context;
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

    @Override
    public MovementState updateState(PlayerContext context, MovementState state) {
        state = super.updateState(context, state);

        state.setInput(Input.SNEAK, false);

        if (state.getStatus() != MovementStatus.RUNNING) {
            // todo
        }

        if (pathContext.canWalkOn(dest.down())) {
            if (context.pos().equals(dest)) {
                return state.setStatus(MovementStatus.SUCCESS);
            }

            state.setInput(Input.SPRINT, true);

        } else {
            throw new AssertionError();
        }

        return state;
    }
}
