package rocks.spaghetti.tedium.ai.path;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import rocks.spaghetti.tedium.util.DefaultedHashMap;
import rocks.spaghetti.tedium.util.Util;

import java.util.*;

public class AStar implements Pathfinder {
    private static final int MAX_DISTANCE = 32;
    private static final Direction[] HORIZONTAL_DIRECTIONS = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };

    private final PriorityQueue<Vec3i> openSet = new PriorityQueue<>(Comparator.comparingDouble(this::score));
    private final Map<Vec3i, Double> gScore = new DefaultedHashMap<>(Double.POSITIVE_INFINITY);
    private final Map<Vec3i, Double> fScore = new DefaultedHashMap<>(Double.POSITIVE_INFINITY);
    private final Map<Vec3i, Vec3i> cameFrom = new HashMap<>();
    private Vec3i last = null;

    private Vec3i start;
    private Vec3i goal;

    public Optional<List<Vec3i>> findPath(Vec3i start, Vec3i goal) {
        this.start = start;
        this.goal = goal;

        openSet.add(start);
        gScore.put(start, 0.0D);
        fScore.put(start, heuristic(start));

        while (!openSet.isEmpty()) {
            Vec3i current = openSet.peek();

            if (current.equals(goal)) {
                return reconstructPath(current);
            }

            openSet.remove(current);
            for (Vec3i neighbor : neighbors(current)) {
                double tentativeScore = gScore.get(current) + weight(current, neighbor);
                if (tentativeScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeScore);
                    fScore.put(neighbor, tentativeScore + heuristic(neighbor));
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }

            last = current;
        }

        return Optional.empty();
    }

    private List<Vec3i> neighbors(Vec3i node) {
        return Arrays.asList(
                node.offset(Direction.UP, 1),
                node.offset(Direction.DOWN, 1),
                node.offset(Direction.NORTH, 1),
                node.offset(Direction.SOUTH, 1),
                node.offset(Direction.EAST, 1),
                node.offset(Direction.WEST, 1)
        );
    }

    private double score(Vec3i node) {
        return start.getManhattanDistance(node) + heuristic(node);
    }

    private double heuristic(Vec3i node) {
        return start.getManhattanDistance(goal);
    }

    private double weight(Vec3i node, Vec3i neighbor) {
        if (start.getManhattanDistance(neighbor) > MAX_DISTANCE) {
            return Double.POSITIVE_INFINITY;
        }

        PlayerEntity player = MinecraftClient.getInstance().player;
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null && player != null) {
            BlockPos neighborPos = new BlockPos(neighbor);
            BlockState neighborBlock = world.getBlockState(neighborPos);
            BlockState aboveBlock = world.getBlockState(neighborPos.up());
            BlockState belowBlock = world.getBlockState(neighborPos.down());

            if (neighborBlock.isSolidBlock(world, neighborPos)
                    || aboveBlock.isSolidBlock(world, neighborPos.up())
                    || (!belowBlock.isSolidBlock(world, neighborPos.down()) && !last.equals(neighborPos.down()))
            ) {
                // cant walk thru solid blocks
                return Double.POSITIVE_INFINITY;
            } else {
                int diff = neighbor.getY() - node.getY();
                if (diff > 0) {
                    for (Direction dir : HORIZONTAL_DIRECTIONS) {
                        BlockPos jumpPos = Util.applyOffsetWithFacing(dir, neighborPos, new Vec3i(1, -1, 0));
                        BlockState jumpGround = world.getBlockState(jumpPos);
                        if (jumpGround.isSolidBlock(world, jumpPos)) {
                            return node.getManhattanDistance(jumpPos);
                        }
                    }

                    // no valid jump destination
                    return Double.POSITIVE_INFINITY;
                } else if (diff < 0) {
                    int max = 3;
                    for (int i = 1; i <= max; i++) {
                        BlockPos fallPos = new BlockPos(neighborPos).down();
                        BlockState fallGround = world.getBlockState(fallPos);
                        if (fallGround.isSolidBlock(world, fallPos)) {
                            return node.getManhattanDistance(fallPos);
                        }
                    }
                    return Double.POSITIVE_INFINITY;
                } else {
                    return node.getManhattanDistance(neighbor);
                }
            }
        }

        return Double.POSITIVE_INFINITY;
    }

    private Optional<List<Vec3i>> reconstructPath(Vec3i node) {
        List<Vec3i> path = new ArrayList<>();
        path.add(0, node);

        while (cameFrom.containsKey(node)) {
            node = cameFrom.get(node);
            path.add(0, node);
        }

        return Optional.of(path);
    }
}
