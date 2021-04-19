package rocks.spaghetti.tedium.ai.movement;

import net.minecraft.util.math.BlockPos;
import rocks.spaghetti.tedium.ai.path.MoveResult;
import rocks.spaghetti.tedium.ai.path.PathContext;
import rocks.spaghetti.tedium.ai.path.PathNode;

public enum Moves {
    NORTH(0, 0, -1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementHorizontal(context, src, src.north());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementHorizontal.cost(context, node.asBlockPos(), node.asBlockPos().north());
        }
    },

    SOUTH(0, 0, +1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementHorizontal(context, src, src.south());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementHorizontal.cost(context, node.asBlockPos(), node.asBlockPos().south());
        }
    },

    EAST(+1, 0, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementHorizontal(context, src, src.east());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementHorizontal.cost(context, node.asBlockPos(), node.asBlockPos().east());
        }
    },

    WEST(-1, 0, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementHorizontal(context, src, src.west());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementHorizontal.cost(context, node.asBlockPos(), node.asBlockPos().west());
        }
    },

    /*UP_NORTH(0, +1, -1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementAscending(context, src, src.up().north());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementAscending.cost(context, node.asBlockPos(), node.asBlockPos().up().north());
        }
    },

    UP_SOUTH(0, +1, +1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementAscending(context, src, src.up().south());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementAscending.cost(context, node.asBlockPos(), node.asBlockPos().up().south());
        }
    },

    UP_EAST(+1, +1, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementAscending(context, src, src.up().east());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementAscending.cost(context, node.asBlockPos(), node.asBlockPos().up().east());
        }
    },

    UP_WEST(-1, +1, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementAscending(context, src, src.up().west());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementAscending.cost(context, node.asBlockPos(), node.asBlockPos().up().west());
        }
    },

    DOWN_NORTH(0, -1, -1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementDescending(context, src, src.down().north());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementDescending.cost(context, node.asBlockPos(), node.asBlockPos().down().north());
        }
    },

    DOWN_SOUTH(0, -1, +1) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementDescending(context, src, src.down().south());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementDescending.cost(context, node.asBlockPos(), node.asBlockPos().down().south());
        }
    },

    DOWN_EAST(+1, -1, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementDescending(context, src, src.down().east());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementDescending.cost(context, node.asBlockPos(), node.asBlockPos().down().east());
        }
    },

    DOWN_WEST(-1, -1, 0) {
        @Override
        public Movement apply(PathContext context, BlockPos src) {
            return new MovementDescending(context, src, src.down().west());
        }

        @Override
        public double cost(PathContext context, PathNode node) {
            return MovementDescending.cost(context, node.asBlockPos(), node.asBlockPos().down().west());
        }
    },*/
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
