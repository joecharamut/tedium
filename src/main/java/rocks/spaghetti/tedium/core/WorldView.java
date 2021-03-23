package rocks.spaghetti.tedium.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import rocks.spaghetti.tedium.Log;

import java.util.HashMap;
import java.util.Map;

public class WorldView {
    public Map<BlockPos, Block> blocks = new HashMap<>();

    public void update() {
        long startTime = System.nanoTime();
        int radius = 8;

        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d pos = client.player.getPos();

        // round off position
        pos = new Vec3d(
                Math.round(pos.x * 10f) / 10f,
                Math.round(pos.y * 10f) / 10f,
                Math.round(pos.z * 10f) / 10f
        );

        for (double dx = -radius; dx <= radius; dx++) {
            for (double dy = -radius; dy <= radius; dy++) {
                for (double dz = -radius; dz <= radius; dz++) {
                    BlockPos blockPos = new BlockPos(pos.add(dx, dy, dz));
                    BlockState blockState = client.world.getBlockState(blockPos);
                    blocks.put(blockPos, blockState.getBlock());
                }
            }
        }

        long endTime = System.nanoTime();
        Log.trace("Time for world update (radius {}): {} ms.", radius, ((endTime - startTime) / 1000000f));
    }
}
