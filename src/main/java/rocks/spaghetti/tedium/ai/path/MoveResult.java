package rocks.spaghetti.tedium.ai.path;

import rocks.spaghetti.tedium.ai.movement.ActionCosts;

public class MoveResult {
    public int x;
    public int y;
    public int z;
    public double cost;

    public MoveResult() {
        reset();
    }

    public void reset() {
        x = 0;
        y = 0;
        z = 0;
        cost = ActionCosts.INFINITY;
    }
}
