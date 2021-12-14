package rocks.spaghetti.tedium.ai.path;

import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class PathContext {
    private final ClientWorld world;
    public final int maxY;
    public final int minY;

    public PathContext(ClientWorld world) {
        this.world = world;
        this.maxY = world.getTopY();
        this.minY = world.getBottomY();
    }

    public BlockState get(int x, int y, int z) {
        return get(new BlockPos(x, y, z));
    }

    public BlockState get(BlockPos pos) {
        return world.getBlockState(pos);
    }

    public boolean canWalkThrough(BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block == Blocks.AIR) return true;
        return !state.isFullCube(world, pos);
    }

    public boolean canWalkThrough(BlockPos pos) {
        return canWalkThrough(get(pos), pos);
    }

    public boolean canWalkOn(BlockPos pos) {
        return canWalkOn(get(pos), pos);
    }

    public boolean canWalkOn(BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block == Blocks.AIR) return false;
        if (block == Blocks.MAGMA_BLOCK) return false;
        if (state.isFullCube(world, pos)) return true;

        if (block == Blocks.FARMLAND || block == Blocks.DIRT_PATH) return true;
        if (block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST) return true;
        if (block == Blocks.GLASS || block instanceof StainedGlassBlock) return true;
        if (block instanceof SlabBlock) {
            SlabType slabType = state.get(SlabBlock.TYPE);
            return (slabType == SlabType.DOUBLE || slabType == SlabType.TOP);
        }

        return false;
    }
}
