package rocks.spaghetti.tedium.ai.movement;

public class ActionCosts {
    private ActionCosts() { throw new IllegalStateException("Utility Class"); }

    public static final double INFINITY = 1_000_000;

    public static final double WALK_COST = 20 / 4.317;
//    public static final double JUMP_COST = 2;

    public static final double WALK_OFF_BLOCK_COST = WALK_COST * 0.8;
    public static final double WALK_ON_SOUL_SAND_COST = WALK_COST * 2;
    public static final double CENTER_AFTER_FALLING_COST = WALK_COST - WALK_OFF_BLOCK_COST;

    public static final double[] FALL_N_BLOCKS_COST = generateFallCosts();
    public static final double FALL_1_25_BLOCKS_COST = distanceToCost(1.25);
    public static final double FALL_0_25_BLOCKS_COST = distanceToCost(0.25);
    public static final double JUMP_COST = FALL_1_25_BLOCKS_COST - FALL_0_25_BLOCKS_COST;

    private static double[] generateFallCosts() {
        double[] costs = new double[256];
        for (int i = 0; i < costs.length; i++) {
            costs[i] = distanceToCost(i);
        }
        return costs;
    }

    private static double distanceToCost(double distance) {
        if (distance == 0) return 0;
        if (distance < 0) throw new IllegalArgumentException("Distance must be >= 0");

        int ticks = 0;
        while (true) {
            double fallDistance = (Math.pow(0.98, ticks) - 1) * -3.92;
            if (distance <= fallDistance) {
                return ticks + distance / fallDistance;
            }
            distance -= fallDistance;
            ticks++;
        }
    }
}
