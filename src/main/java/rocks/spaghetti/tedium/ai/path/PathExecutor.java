package rocks.spaghetti.tedium.ai.path;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.player.PlayerContext;
import rocks.spaghetti.tedium.ai.movement.Movement;
import rocks.spaghetti.tedium.ai.movement.MovementStatus;

public class PathExecutor {
    private final Path path;
    private final ClientPlayerEntity player;
    private final PlayerContext playerContext;

    private int pathIndex = 0;
    private boolean finished = false;

    private Runnable onStop = null;
    private Runnable onSuccess = null;
    private Runnable onError = null;

    public PathExecutor(Path path) {
        this.path = path;
        if (!this.path.processMovements()) {
            throw new IllegalStateException("Movement Processing Failed");
        }
        this.player = MinecraftClient.getInstance().player;
        this.playerContext = new PlayerContext();
    }

    public PathExecutor onStop(Runnable callback) {
        onStop = callback;
        return this;
    }

    public PathExecutor onSuccess(Runnable callback) {
        onSuccess = callback;
        return this;
    }

    public PathExecutor onError(Runnable callback) {
        onError = callback;
        return this;
    }

    private void stop() {
        finished = true;
        if (onStop != null) onStop.run();
    }

    private void success() {
        if (onSuccess != null) onSuccess.run();
        stop();
    }

    private void error() {
        if (onError != null) onError.run();
        stop();
    }

    public void tick() {
        if (finished) return;
        if (pathIndex >= path.getMovements().size()) {
            success();
            return;
        }

        Movement movement = path.getMovements().get(pathIndex);
        BlockPos currentPos = player.getBlockPos();

        if (path.goal.isInGoal(currentPos)) {
            success();
            return;
        }

        MovementStatus status = movement.update(playerContext);

        if (status == MovementStatus.FAILED) {
            error();
            return;
        }

        if (status.complete) {
            pathIndex++;
            return;
        }
    }
}
