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

    private Runnable onFinish = null;

    public PathExecutor(Path path) {
        this.path = path;
        if (!this.path.processMovements()) {
            throw new IllegalStateException("Movement Processing Failed");
        }
        this.player = MinecraftClient.getInstance().player;
        this.playerContext = new PlayerContext();
    }

    public PathExecutor onFinish(Runnable callback) {
        onFinish = callback;
        return this;
    }

    private void finish() {
        finished = true;
        if (onFinish != null) onFinish.run();
    }

    public void tick() {
        if (finished) return;
        if (pathIndex >= path.getMovements().size()) {
            finish();
            return;
        }

        Movement movement = path.getMovements().get(pathIndex);
        BlockPos currentPos = player.getBlockPos();

        if (path.goal.isInGoal(currentPos)) {
            finish();
            return;
        }

        MovementStatus status = movement.update(playerContext);
        if (status.complete) {
            pathIndex++;
            return;
        }
    }
}
