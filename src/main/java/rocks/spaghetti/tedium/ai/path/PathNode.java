package rocks.spaghetti.tedium.ai.path;

import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.goals.Goal;
import rocks.spaghetti.tedium.ai.movement.ActionCosts;

import java.util.Objects;

public class PathNode {
    public final int x;
    public final int y;
    public final int z;

    public final double estimatedCostToGoal;

    public double cost;
    public double combinedCost;
    public PathNode previous;

    public PathNode(int x, int y, int z, Goal goal) {
        this.cost = ActionCosts.INFINITY;
        this.estimatedCostToGoal = goal.heuristic(x, y, z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos asBlockPos() {
        return new BlockPos(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("PathNode{x=%d, y=%d, z=%d, cost=%s}", x, y, z, cost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PathNode)) return false;

        PathNode other = (PathNode) o;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
