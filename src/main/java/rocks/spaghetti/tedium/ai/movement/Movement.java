package rocks.spaghetti.tedium.ai.movement;

import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.player.PlayerContext;
import rocks.spaghetti.tedium.ai.path.PathContext;

public abstract class Movement {
    protected final BlockPos src;
    protected final BlockPos dest;
    private MovementState currentState = new MovementState().setStatus(MovementStatus.INITIAL);

    public Movement(BlockPos src, BlockPos dest) {
        this.src = src;
        this.dest = dest;
    }

    public abstract double calculateCost(PathContext context);

    public BlockPos getSrc() {
        return src;
    }

    public BlockPos getDest() {
        return dest;
    }

    private boolean prepared(MovementState state) {
        if (state.getStatus() == MovementStatus.WAITING) return true;
        return false;
    }

    public MovementState updateState(PlayerContext context, MovementState state) {
        if (!prepared(state)) {
            return state.setStatus(MovementStatus.PREPARING);
        }

        if (state.getStatus() == MovementStatus.PREPARING) {
            state.setStatus(MovementStatus.WAITING);
        }

        if (state.getStatus() == MovementStatus.WAITING) {
            state.setStatus(MovementStatus.RUNNING);
        }

        return state;
    }

    public MovementStatus update(PlayerContext context) {
        currentState = updateState(context, currentState);

        if (context.player().isTouchingWater()) {
            context.controls().jumping(true);
        }

        currentState.getLookTarget().ifPresent(rotation -> context.controls().look(rotation.pitch, rotation.yaw));

        float moveX = 0;
        float moveZ = 0;
        moveX += currentState.getInput(Input.FORWARD) ? 1.0 : 0.0;
        moveX += currentState.getInput(Input.BACKWARD) ? -1.0 : 0.0;
        moveZ += currentState.getInput(Input.RIGHT) ? 1.0 : 0.0;
        moveZ += currentState.getInput(Input.LEFT) ? -1.0 : 0.0;
        context.controls().movement(moveX, moveZ);

        context.controls().jumping(currentState.getInput(Input.JUMP));
        context.controls().sprinting(currentState.getInput(Input.SPRINT));
        context.controls().sneaking(currentState.getInput(Input.SNEAK));

        currentState.clearInputs();

        if (currentState.getStatus().complete) {
            context.controls().clear();
        }

        return currentState.getStatus();
    }
}
