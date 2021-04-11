package rocks.spaghetti.tedium.ai.goals;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class GoalBlock implements Goal {
    public final int x;
    public final int y;
    public final int z;

    public GoalBlock(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return x == this.x && y == this.y && z == this.z;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        int dx = x - this.x;
        int dy = y - this.y;
        int dz = z - this.z;

        double ax = Math.abs(dx);
        double az = Math.abs(dz);
        double straight;
        double diagonal;
        if (x < z) {
            straight = az - ax;
            diagonal = ax;
        } else {
            straight = ax - az;
            diagonal = az;
        }
        diagonal *= MathHelper.SQUARE_ROOT_OF_TWO;

        // todo y cost

        return straight + diagonal;
    }

    @Override
    public String toString() {
        return String.format("GoalBlock{x=%s, y=%s, z=%s}", x, y, z);
    }
}
