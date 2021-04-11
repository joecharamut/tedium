package rocks.spaghetti.tedium.ai.movement;

import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.player.PlayerContext;
import rocks.spaghetti.tedium.ai.path.PathContext;

public abstract class Movement {
    protected final BlockPos src;
    protected final BlockPos dest;

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

    public MovementStatus update(PlayerContext context) {
        if (context.pos().equals(dest)) {
            return MovementStatus.SUCCESS;
        }
        return MovementStatus.WAITING;
    }
}
