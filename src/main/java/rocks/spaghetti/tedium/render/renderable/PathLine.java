package rocks.spaghetti.tedium.render.renderable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static rocks.spaghetti.tedium.render.RenderHelper.drawLines;

public class PathLine implements Renderable {
    private final Vec3d[] blocks;
    private final int color;

    public PathLine(List<BlockPos> blocks, int color) {
        this.blocks = blocks.stream().map(Vec3d::ofCenter).toArray(Vec3d[]::new);
        this.color = color;
    }

    @Override
    public void render(WorldRenderContext context) {
        RenderSystem.assertOnGameThread();

        float r = ((color >> 16) & 255) / 255.0f;
        float g = ((color >>  8) & 255) / 255.0f;
        float b = ((color >>  0) & 255) / 255.0f;

        RenderSystem.lineWidth(1.5f);

        Vec3d prev = null;
        Vec3d cameraPos = context.camera().getPos();

        for (Vec3d current : blocks) {
            if (prev != null) {
                double fromX = prev.x - cameraPos.x;
                double fromY = prev.y - cameraPos.y;
                double fromZ = prev.z - cameraPos.z;

                double toX = current.x - cameraPos.x;
                double toY = current.y - cameraPos.y;
                double toZ = current.z - cameraPos.z;

                Tessellator tess = Tessellator.getInstance();
                BufferBuilder buffer = tess.getBuffer();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);

                drawLines(tess, buffer, new double[][]{{fromX, fromY, fromZ}, {toX, toY, toZ}}, r, g, b, 1.0f);
            }

            prev = current;
        }
    }
}
