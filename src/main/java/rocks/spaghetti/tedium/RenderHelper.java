package rocks.spaghetti.tedium;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderHelper {
    private RenderHelper() { throw new IllegalStateException("Utility Class"); }

    public static void renderEventHandler(WorldRenderContext context) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        outlineBlock(context.camera(), new BlockPos(206, 65, -118), 0xff00ff, true, false);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public static void outlineBlock(Camera camera, BlockPos pos, int color, boolean lines, boolean fill) {
        double cameraX = camera.getPos().x;
        double cameraY = camera.getPos().y;
        double cameraZ = camera.getPos().z;

        float r = ((color >> 16) & 255) / 255.0f;
        float g = ((color >>  8) & 255) / 255.0f;
        float b = ((color >>  0) & 255) / 255.0f;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        double adjX = x - cameraX;
        double adjY = y - cameraY;
        double adjZ = z - cameraZ;

        RenderSystem.lineWidth(0.5f);

        // corner
        glRenderPolygon(new double[][]{
                {adjX + 0.1, adjY, adjZ + 0.1},
                {adjX + 0.1, adjY, adjZ},
                {adjX, adjY, adjZ},
                {adjX, adjY, adjZ + 0.1}
        }, 0, 1, 0, 1.0f);

        if (lines) {
            // bottom face
            glRenderLine(new double[][]{
                    {adjX, adjY, adjZ}, {adjX + 1, adjY, adjZ},
                    {adjX, adjY, adjZ}, {adjX, adjY, adjZ + 1},
                    {adjX + 1, adjY, adjZ}, {adjX + 1, adjY, adjZ + 1},
                    {adjX, adjY, adjZ + 1}, {adjX + 1, adjY, adjZ + 1}
            }, r, g, b, 1.0f);

            // top face
            glRenderLine(new double[][]{
                    {adjX, adjY + 1, adjZ}, {adjX + 1, adjY + 1, adjZ},
                    {adjX, adjY + 1, adjZ}, {adjX, adjY + 1, adjZ + 1},
                    {adjX + 1, adjY + 1, adjZ}, {adjX + 1, adjY + 1, adjZ + 1},
                    {adjX, adjY + 1, adjZ + 1}, {adjX + 1, adjY + 1, adjZ + 1}
            }, r, g, b, 1.0f);

            // north face
            glRenderLine(new double[][]{
                    {adjX, adjY, adjZ}, {adjX + 1, adjY, adjZ},
                    {adjX, adjY, adjZ}, {adjX, adjY + 1, adjZ},
                    {adjX + 1, adjY, adjZ}, {adjX + 1, adjY + 1, adjZ},
                    {adjX + 1, adjY + 1, adjZ}, {adjX, adjY + 1, adjZ}
            }, r, g, b, 1.0f);

            // east face
            glRenderLine(new double[][]{
                    {adjX + 1, adjY, adjZ}, {adjX + 1, adjY, adjZ + 1},
                    {adjX + 1, adjY, adjZ}, {adjX + 1, adjY + 1, adjZ},
                    {adjX + 1, adjY, adjZ + 1}, {adjX + 1, adjY + 1, adjZ + 1},
                    {adjX + 1, adjY + 1, adjZ + 1}, {adjX + 1, adjY + 1, adjZ}
            }, r, g, b, 1.0f);

            // south face
            glRenderLine(new double[][]{
                    {adjX, adjY, adjZ + 1}, {adjX + 1, adjY, adjZ + 1},
                    {adjX, adjY, adjZ + 1}, {adjX, adjY + 1, adjZ + 1},
                    {adjX + 1, adjY, adjZ + 1}, {adjX + 1, adjY + 1, adjZ + 1},
                    {adjX, adjY + 1, adjZ + 1}, {adjX + 1, adjY + 1, adjZ + 1}
            }, r, g, b, 1.0f);

            // west face
            glRenderLine(new double[][]{
                    {adjX, adjY, adjZ}, {adjX, adjY, adjZ + 1},
                    {adjX, adjY, adjZ}, {adjX, adjY + 1, adjZ},
                    {adjX, adjY, adjZ + 1}, {adjX, adjY + 1, adjZ + 1},
                    {adjX, adjY + 1, adjZ + 1}, {adjX, adjY + 1, adjZ}
            }, r, g, b, 1.0f);
        }

        if (fill) {
            // bottom face
            glRenderPolygon(new double[][]{
                    {adjX + 1, adjY, adjZ + 1},
                    {adjX + 1, adjY, adjZ},
                    {adjX, adjY, adjZ},
                    {adjX, adjY, adjZ + 1}
            }, r, g, b, 0.25f);

            // top face
            glRenderPolygon(new double[][]{
                    {adjX + 1, adjY + 1, adjZ + 1},
                    {adjX + 1, adjY + 1, adjZ},
                    {adjX, adjY + 1, adjZ},
                    {adjX, adjY + 1, adjZ + 1}
            }, r, g, b, 0.25f);

            // north face
            glRenderPolygon(new double[][]{
                    {adjX + 1, adjY + 1, adjZ},
                    {adjX + 1, adjY, adjZ},
                    {adjX, adjY, adjZ},
                    {adjX, adjY + 1, adjZ}
            }, r, g, b, 0.25f);

            // east face
            glRenderPolygon(new double[][]{
                    {adjX + 1, adjY + 1, adjZ + 1},
                    {adjX + 1, adjY, adjZ + 1},
                    {adjX + 1, adjY, adjZ},
                    {adjX + 1, adjY + 1, adjZ}
            }, r, g, b, 0.25f);

            // south face
            glRenderPolygon(new double[][]{
                    {adjX + 1, adjY + 1, adjZ + 1},
                    {adjX + 1, adjY, adjZ + 1},
                    {adjX, adjY, adjZ + 1},
                    {adjX, adjY + 1, adjZ + 1}
            }, r, g, b, 0.25f);

            // west face
            glRenderPolygon(new double[][]{
                    {adjX, adjY + 1, adjZ + 1},
                    {adjX, adjY, adjZ + 1},
                    {adjX, adjY, adjZ},
                    {adjX, adjY + 1, adjZ}
            }, r, g, b, 0.25f);
        }
    }

    private static void glRenderLine(double[][] vertexes, float r, float g, float b, float a) {
        GL11.glBegin(GL11.GL_LINES);

        GL11.glColor4f(r, g, b, a);
        for (double[] coords : vertexes) {
            GL11.glVertex3dv(coords);
        }

        GL11.glEnd();
    }

    private static void glRenderPolygon(double[][] vertexes, float r, float g, float b, float a) {
        GL11.glBegin(GL11.GL_POLYGON);

        GL11.glColor4f(r, g, b, a);
        for (double[] coords : vertexes) {
            GL11.glVertex3dv(coords);
        }

        GL11.glEnd();
    }
}
