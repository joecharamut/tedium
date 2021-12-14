package rocks.spaghetti.tedium.render.renderable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static rocks.spaghetti.tedium.render.RenderHelper.glRenderLines;
import static rocks.spaghetti.tedium.render.RenderHelper.glRenderPolygon;

public class OutlineRegion implements Renderable {
    private static final Vec3d UNIT_VECTOR = new Vec3d(1, 1, 1);

    private final BlockPos origin;
    private final Vec3d size;
    private final int color;
    private final boolean lines;
    private final boolean fill;

    public OutlineRegion(BlockPos origin, Vec3d size, int color, boolean lines, boolean fill) {
        this.origin = origin;
        this.size = size;
        this.color = color;
        this.lines = lines;
        this.fill = fill;
    }

    public OutlineRegion(BlockPos origin, int color, boolean fill) {
        this(origin, UNIT_VECTOR, color, true, fill);
    }

    public OutlineRegion(BlockPos origin, int color) {
        this(origin, UNIT_VECTOR, color, true, false);
    }

    @Override
    public void render(WorldRenderContext context) {
        RenderSystem.assertOnGameThread();

        if (MinecraftClient.getInstance().player == null) return;
        if (MinecraftClient.getInstance().player.squaredDistanceTo(origin.getX(), origin.getY(), origin.getZ())
                > context.gameRenderer().getViewDistance() * context.gameRenderer().getViewDistance()) return;

        outlineRegion(context, origin, size, color, lines, fill);
    }

    private static void outlineRegion(WorldRenderContext context, BlockPos origin, Vec3d size, int color, boolean lines, boolean fill) {
        Camera camera = context.camera();

        float r = ((color >> 16) & 255) / 255.0f;
        float g = ((color >>  8) & 255) / 255.0f;
        float b = ((color >>  0) & 255) / 255.0f;

        double l = size.getX();
        double h = size.getY();
        double w = size.getZ();

        double adjX = origin.getX() - camera.getPos().getX();
        double adjY = origin.getY() - camera.getPos().getY();
        double adjZ = origin.getZ() - camera.getPos().getZ();

        RenderSystem.lineWidth(1.5f);

        if (lines) {
            // bottom face
            glRenderLines(new double[][]{
                    {adjX, adjY, adjZ}, {adjX + l, adjY, adjZ},
                    {adjX, adjY, adjZ}, {adjX, adjY, adjZ + w},
                    {adjX + l, adjY, adjZ}, {adjX + l, adjY, adjZ + w},
                    {adjX, adjY, adjZ + w}, {adjX + l, adjY, adjZ + w}
            }, r, g, b, 1.0f);

            // top face
            glRenderLines(new double[][]{
                    {adjX, adjY + h, adjZ}, {adjX + l, adjY + h, adjZ},
                    {adjX, adjY + h, adjZ}, {adjX, adjY + h, adjZ + w},
                    {adjX + l, adjY + h, adjZ}, {adjX + l, adjY + h, adjZ + w},
                    {adjX, adjY + h, adjZ + w}, {adjX + l, adjY + h, adjZ + w}
            }, r, g, b, 1.0f);

            // north face
            glRenderLines(new double[][]{
                    {adjX, adjY, adjZ}, {adjX + l, adjY, adjZ},
                    {adjX, adjY, adjZ}, {adjX, adjY + h, adjZ},
                    {adjX + l, adjY, adjZ}, {adjX + l, adjY + h, adjZ},
                    {adjX + l, adjY + h, adjZ}, {adjX, adjY + h, adjZ}
            }, r, g, b, 1.0f);

            // east face
            glRenderLines(new double[][]{
                    {adjX + l, adjY, adjZ}, {adjX + l, adjY, adjZ + w},
                    {adjX + l, adjY, adjZ}, {adjX + l, adjY + h, adjZ},
                    {adjX + l, adjY, adjZ + w}, {adjX + l, adjY + h, adjZ + w},
                    {adjX + l, adjY + h, adjZ + w}, {adjX + l, adjY + h, adjZ}
            }, r, g, b, 1.0f);

            // south face
            glRenderLines(new double[][]{
                    {adjX, adjY, adjZ + w}, {adjX + l, adjY, adjZ + w},
                    {adjX, adjY, adjZ + w}, {adjX, adjY + h, adjZ + w},
                    {adjX + l, adjY, adjZ + w}, {adjX + l, adjY + h, adjZ + w},
                    {adjX, adjY + h, adjZ + w}, {adjX + l, adjY + h, adjZ + w}
            }, r, g, b, 1.0f);

            // west face
            glRenderLines(new double[][]{
                    {adjX, adjY, adjZ}, {adjX, adjY, adjZ + w},
                    {adjX, adjY, adjZ}, {adjX, adjY + h, adjZ},
                    {adjX, adjY, adjZ + w}, {adjX, adjY + h, adjZ + w},
                    {adjX, adjY + h, adjZ + w}, {adjX, adjY + h, adjZ}
            }, r, g, b, 1.0f);
        }

        if (fill) {
            // bottom face
            glRenderPolygon(new double[][]{
                    {adjX + l, adjY, adjZ + w},
                    {adjX + l, adjY, adjZ},
                    {adjX, adjY, adjZ},
                    {adjX, adjY, adjZ + w}
            }, r, g, b, 0.25f);

            // top face
            glRenderPolygon(new double[][]{
                    {adjX + l, adjY + h, adjZ + w},
                    {adjX + l, adjY + h, adjZ},
                    {adjX, adjY + h, adjZ},
                    {adjX, adjY + h, adjZ + w}
            }, r, g, b, 0.25f);

            // north face
            glRenderPolygon(new double[][]{
                    {adjX + l, adjY + h, adjZ},
                    {adjX + l, adjY, adjZ},
                    {adjX, adjY, adjZ},
                    {adjX, adjY + h, adjZ}
            }, r, g, b, 0.25f);

            // east face
            glRenderPolygon(new double[][]{
                    {adjX + l, adjY + h, adjZ + w},
                    {adjX + l, adjY, adjZ + w},
                    {adjX + l, adjY, adjZ},
                    {adjX + l, adjY + h, adjZ}
            }, r, g, b, 0.25f);

            // south face
            glRenderPolygon(new double[][]{
                    {adjX + l, adjY + h, adjZ + w},
                    {adjX + l, adjY, adjZ + w},
                    {adjX, adjY, adjZ + w},
                    {adjX, adjY + h, adjZ + w}
            }, r, g, b, 0.25f);

            // west face
            glRenderPolygon(new double[][]{
                    {adjX, adjY + h, adjZ + w},
                    {adjX, adjY, adjZ + w},
                    {adjX, adjY, adjZ},
                    {adjX, adjY + h, adjZ}
            }, r, g, b, 0.25f);
        }
    }
}
