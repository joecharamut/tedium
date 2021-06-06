package rocks.spaghetti.tedium.ai.movement;

import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.movement.moves.MovementAscend;
import rocks.spaghetti.tedium.ai.movement.moves.MovementDescend;
import rocks.spaghetti.tedium.ai.movement.moves.MovementHorizontal;
import rocks.spaghetti.tedium.ai.path.MoveResult;
import rocks.spaghetti.tedium.ai.path.PathContext;
import rocks.spaghetti.tedium.ai.path.PathNode;

public enum Moves {
    NORTH(0, 0, -1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementHorizontal(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementHorizontal.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    EAST(+1, 0, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementHorizontal(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementHorizontal.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    SOUTH(0, 0, +1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementHorizontal(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementHorizontal.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    WEST(-1, 0, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementHorizontal(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementHorizontal.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    UP_NORTH(0, +1, -1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementAscend(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementAscend.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    UP_SOUTH(0, +1, +1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementAscend(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementAscend.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    UP_EAST(+1, +1, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementAscend(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementAscend.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    UP_WEST(-1, +1, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementAscend(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementAscend.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    DOWN_NORTH(0, -1, -1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementDescend(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementDescend.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    DOWN_SOUTH(0, -1, +1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementDescend(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementDescend.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    DOWN_EAST(+1, -1, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementDescend(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementDescend.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },

    DOWN_WEST(-1, -1, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementDescend(context, src, src.add(xOffset, yOffset, zOffset));
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            BlockPos pos = node.asBlockPos();
            return MovementDescend.cost(context, pos, pos.add(xOffset, yOffset, zOffset));
        }
    },
    ;

    public final int xOffset;
    public final int yOffset;
    public final int zOffset;

    Moves(int x, int y, int z) {
        this.xOffset = x;
        this.yOffset = y;
        this.zOffset = z;
    }

    public abstract Movement apply(PathContext context, BlockPos src);

    public void consider(PathContext context, PathNode node, MoveResult result) {
        result.x = node.x + xOffset;
        result.y = node.y + yOffset;
        result.z = node.z + zOffset;
        result.cost = cost(context, node);
    }

    public abstract double cost(PathContext context, PathNode node);
}
