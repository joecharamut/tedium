package rocks.spaghetti.tedium.ai.goals;

import net.minecraft.util.math.BlockPos;

public interface Goal {
    boolean isInGoal(int x, int y, int z);
    default boolean isInGoal(BlockPos pos) {
        return isInGoal(pos.getX(), pos.getY(), pos.getZ());
    }

    double heuristic(int x, int y, int z);
}
