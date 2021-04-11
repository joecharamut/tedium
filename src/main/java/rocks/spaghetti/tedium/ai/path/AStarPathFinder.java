package rocks.spaghetti.tedium.ai.path;

import net.minecraft.util.math.Vec3i;
import rocks.spaghetti.tedium.ClientEntrypoint;
import rocks.spaghetti.tedium.ai.goals.Goal;
import rocks.spaghetti.tedium.ai.movement.ActionCosts;
import rocks.spaghetti.tedium.ai.movement.Moves;
import rocks.spaghetti.tedium.util.Log;

import java.util.*;

public class AStarPathFinder {
    private final Vec3i start;
    private final Goal goal;
    private final PathContext context;
    private final Map<Integer, PathNode> pathMap = new HashMap<>();

    private PathNode startNode;
    private PathNode mostRecent;

    private static final double[] A_STAR_COEFFICIENTS = {1.5, 2, 2.5, 3, 4, 5, 10};
    private final PathNode[] best = new PathNode[A_STAR_COEFFICIENTS.length];

    private boolean finished;
    private boolean cancelRequested;

    private static final double MIN_PATH_LENGTH = 5;
    private static final double COST_MINIMUM = 0.01;

    public AStarPathFinder(Vec3i start, Goal goal, PathContext context) {
        this.start = start;
        this.goal = goal;
        this.context = context;
    }

    public void cancel() {
        cancelRequested = true;
    }

    public Optional<Path> calculate(long timeout) {
        if (finished) throw new IllegalStateException("Calculation already finished");

        startNode = getNodeAtPosition(start.getX(), start.getY(), start.getZ());
        startNode.cost = 0;
        startNode.combinedCost = startNode.estimatedCostToGoal;

        PriorityQueue<PathNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(node -> node.combinedCost));
        openSet.add(startNode);

        double[] bestHeuristic = new double[A_STAR_COEFFICIENTS.length];
        for (int i = 0; i < A_STAR_COEFFICIENTS.length; i++) {
            bestHeuristic[i] = startNode.estimatedCostToGoal;
            best[i] = startNode;
        }

        long startTime = System.currentTimeMillis();
        long timeoutTime = timeout != -1 ? startTime + timeout : Long.MAX_VALUE;
        int timeoutCheckInterval = (1 << 6) - 1;

        int nodeCount = 0;
        int movesConsidered = 0;

        MoveResult result = new MoveResult();
        Optional<Path> foundPath = Optional.empty();

        while (!openSet.isEmpty() && !cancelRequested) {
            if ((nodeCount & timeoutCheckInterval) == 0 && System.currentTimeMillis() >= timeoutTime) {
                break;
            }

            PathNode current = openSet.poll();
            mostRecent = current;
            nodeCount++;

            if (goal.isInGoal(current.x, current.y, current.z)) {
                Log.info("Calculated path in {} ms", System.currentTimeMillis() - startTime);
                foundPath = Optional.of(new Path(startNode, current, nodeCount, goal, context));
                break;
            }

            for (Moves move : Moves.values()) {
                int newX = current.x + move.xOffset;
                int newY = current.y + move.yOffset;
                int newZ = current.z + move.zOffset;

                if (newY > context.maxY || newY < context.minY) continue;

                result.reset();
                move.consider(context, current, result);
                movesConsidered++;

                if (result.cost >= ActionCosts.INFINITY) continue;
                if (result.cost <= 0 || Double.isNaN(result.cost)) throw new IllegalStateException("Impossible cost for move");

                PathNode neighbor = getNodeAtPosition(result.x, result.y, result.z);
                double tentativeCost = current.cost + result.cost;

                if (neighbor.cost - tentativeCost > COST_MINIMUM) {
                    neighbor.previous = current;
                    neighbor.cost = tentativeCost;
                    neighbor.combinedCost = tentativeCost + neighbor.estimatedCostToGoal;
                    openSet.remove(neighbor);
                    openSet.add(neighbor);
                }

                for (int i = 0; i < A_STAR_COEFFICIENTS.length; i++) {
                    double heuristic = neighbor.estimatedCostToGoal + (neighbor.cost / A_STAR_COEFFICIENTS[i]);
                    if (bestHeuristic[i] - heuristic > COST_MINIMUM) {
                        bestHeuristic[i] = heuristic;
                        best[i] = neighbor;
                    }
                }
            }
        }

        if (cancelRequested) foundPath = Optional.empty();
        if (!foundPath.isPresent()) {
            foundPath = bestPathSoFar(nodeCount);
            if (foundPath.isPresent()) ClientEntrypoint.sendClientMessage("Pathfinding failed, using best path so far");
        }

        String debug = String.format("Pathfinding %s in %s ms (Considered %s movements, checked %s nodes)",
                foundPath.isPresent() ? "succeeded" : "failed", System.currentTimeMillis() - startTime, movesConsidered, nodeCount);
        Log.info(debug);
        ClientEntrypoint.sendClientMessage(debug);

        return foundPath;
    }

    public double getSquaredDistanceFromStart(PathNode node) {
        int dx = node.x - startNode.x;
        int dy = node.y - startNode.y;
        int dz = node.z - startNode.z;
        return dx*dx + dy*dy + dz*dz;
    }

    public PathNode getNodeAtPosition(int x, int y, int z) {
        int hash = Objects.hash(x, y, z);
        PathNode node = pathMap.get(hash);
        if (node == null) {
            node = new PathNode(x, y, z, goal);
            pathMap.put(hash, node);
        }
        return node;
    }

    public Optional<Path> bestPathSoFar(int nodeCount) {
        if (startNode == null) {
            return Optional.empty();
        }
        double bestDist = 0;
        for (int i = 0; i < A_STAR_COEFFICIENTS.length; i++) {
            if (best[i] == null) {
                continue;
            }
            double dist = getSquaredDistanceFromStart(best[i]);
            if (dist > bestDist) {
                bestDist = dist;
            }
            if (dist > MIN_PATH_LENGTH * MIN_PATH_LENGTH) {
                if (A_STAR_COEFFICIENTS[i] >= 3) {
                    Log.warn("Warning: cost coefficient is greater than three!");
                }
                Log.info("Path goes for {} blocks (A* coefficient: {})", Math.sqrt(dist), A_STAR_COEFFICIENTS[i]);
                return Optional.of(new Path(startNode, best[i], nodeCount, goal, context));
            }
        }

        return Optional.empty();
    }

    public boolean isFinished() {
        return finished;
    }

    public Goal getGoal() {
        return goal;
    }

    public Vec3i getStart() {
        return start;
    }
}
