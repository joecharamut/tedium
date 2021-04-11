package rocks.spaghetti.tedium.ai.path;

import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.goals.Goal;
import rocks.spaghetti.tedium.ai.movement.Movement;
import rocks.spaghetti.tedium.ai.movement.Moves;
import rocks.spaghetti.tedium.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Path {
    public final PathNode startNode;
    public final PathNode endNode;
    public final int nodeCount;
    public final Goal goal;
    public final PathContext context;

    private final List<BlockPos> path;
    private final List<PathNode> nodes;
    private final List<Movement> movements = new ArrayList<>();

    public Path(PathNode startNode, PathNode endNode, int nodeCount, Goal goal, PathContext context) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.nodeCount = nodeCount;
        this.goal = goal;
        this.context = context;

        PathNode current = endNode;
        LinkedList<BlockPos> tmpPath = new LinkedList<>();
        LinkedList<PathNode> tmpNodes = new LinkedList<>();
        while (current != null) {
            tmpPath.addFirst(current.asBlockPos());
            tmpNodes.addFirst(current);
            current = current.previous;
        }
        path = tmpPath;
        nodes = tmpNodes;
    }

    public boolean processMovements() {
        if (path.isEmpty()) throw new IllegalStateException("No Path");
        if (!movements.isEmpty()) throw new IllegalStateException("Already Processed");

        for (int i = 0; i < path.size() - 1; i++) {
            double cost = nodes.get(i + 1).cost - nodes.get(i).cost;
            Movement move = doubleCheckMovement(path.get(i), path.get(i + 1), cost);
            if (move == null) {
                Log.error("Null movement check: {from: {}, to: {}, cost:{}}", path.get(i), path.get(i + 1), cost);
                return false;
            }
            movements.add(move);
        }

        return true;
    }

    private Movement doubleCheckMovement(BlockPos src, BlockPos dest, double cost) {
        for (Moves move : Moves.values()) {
            Movement applied = move.apply(context, src);
            if (applied.getDest().equals(dest)) {
                return applied;
            }
        }

        return null;
    }

    public int length() {
        return path.size();
    }

    public List<BlockPos> getPath() {
        return Collections.unmodifiableList(path);
    }

    public List<PathNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Movement> getMovements() {
        return Collections.unmodifiableList(movements);
    }
}
