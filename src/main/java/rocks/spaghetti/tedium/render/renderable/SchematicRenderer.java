package rocks.spaghetti.tedium.render.renderable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rocks.spaghetti.tedium.schematic.Schematic;

import java.util.LinkedList;
import java.util.List;

public class SchematicRenderer implements Renderable {
    private final BlockPos origin;
    private final BlockRenderManager blockRenderManager;
    private final World world;

    private final BlockState[] blocks;
    private final BlockPos[] positions;

    public SchematicRenderer(Schematic schematic, BlockPos origin) {
        this.origin = origin;
        this.blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        this.world = MinecraftClient.getInstance().world;

        List<BlockPos> tempPositions = new LinkedList<>();
        List<BlockState> tempBlocks = new LinkedList<>();
        schematic.blockData.forEach((pos, state) -> {
            if (state.isAir()) return;

            tempPositions.add(new BlockPos(pos));
            tempBlocks.add(state);
        });
        positions = tempPositions.toArray(new BlockPos[0]);
        blocks = tempBlocks.toArray(new BlockState[0]);
    }

    @Override
    public void render(WorldRenderContext context) {
        RenderSystem.enableDepthTest();

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        Vec3d cameraPos = context.camera().getPos();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        matrices.translate(origin.getX(), origin.getY(), origin.getZ());
        for (int i = 0; i < positions.length; i++) {
            BlockState state = blocks[i];
            BlockPos pos = positions[i];

            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            renderBlock(state, pos, matrices, consumers);
            matrices.pop();
        }
        matrices.pop();

        RenderSystem.disableDepthTest();
    }

    private boolean renderBlock(BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumerProvider consumers) {
        return blockRenderManager.renderBlock(state, pos, world, matrices, consumers.getBuffer(RenderLayers.getBlockLayer(state)), false, world.random);
    }
}
