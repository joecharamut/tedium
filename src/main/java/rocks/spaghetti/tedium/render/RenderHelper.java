package rocks.spaghetti.tedium.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RenderHelper {
    private RenderHelper() { throw new IllegalStateException("Utility Class"); }
    private static final Vec3d UNIT_VECTOR = new Vec3d(1, 1, 1);

    private static final Queue<RenderListener> listeners = new ConcurrentLinkedQueue<>();
    private static final Queue<Renderable> beforeDebugQueue = new ArrayDeque<>();
    private static final Queue<Renderable> afterEntitiesQueue = new ArrayDeque<>();

    public static void clearListeners() {
        listeners.clear();
    }

    public static void addListener(RenderListener listener) {
        listeners.add(listener);
    }

    public static void queue(Renderable... objs) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);

        for (Renderable obj : objs) {
            if (obj instanceof OutlineRegion) {
                beforeDebugQueue.add(obj);
            } else if (obj instanceof FloatingText) {
                afterEntitiesQueue.add(obj);
            } else {
                beforeDebugQueue.add(obj);
            }
        }
    }

    public static void start(WorldRenderContext context) {
        for (RenderListener listener : listeners) {
            listener.onRenderStart();
        }
    }

    public static void afterEntities(WorldRenderContext context) {
        Renderable obj;
        while ((obj = afterEntitiesQueue.poll()) != null) {
            obj.render(context);
        }
    }

    public static void beforeDebugRenderer(WorldRenderContext context) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        Renderable obj;
        while ((obj = beforeDebugQueue.poll()) != null) {
            obj.render(context);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    @FunctionalInterface
    public interface RenderListener {
        void onRenderStart();
    }

    @FunctionalInterface
    public interface Renderable {
        void render(WorldRenderContext context);
    }

    public static class OutlineRegion implements Renderable {
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
            RenderSystem.assertThread(RenderSystem::isOnGameThread);

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

    public static class FloatingText implements Renderable {
        private final Text text;
        private final BlockPos pos;

        public FloatingText(Text text, BlockPos pos) {
            this.text = text;
            this.pos = pos;
        }

        @Override
        public void render(WorldRenderContext context) {
            renderTextLabel(context, text, pos.getX(), pos.getY(), pos.getZ());
        }

        private static void renderTextLabel(WorldRenderContext context, Text text, double x, double y, double z) {
            MatrixStack matrices = context.matrixStack();
            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            matrices.push();

            Vec3d cameraPos = camera.getPos();
            // to origin
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            // to block pos
            matrices.translate(x, y, z);
            // center in block
            matrices.translate(0.5, 0.5, 0.5);
            matrices.multiply(camera.getRotation());
            matrices.scale(-0.025F, -0.025F, 0.025F);
            float halfWidth = -textRenderer.getWidth(text) / 2.0F;
            textRenderer.draw(text, halfWidth, 0, 0xffffffff, false, matrices.peek().getModel(), context.consumers(), false, 0x3f000000, 0xf00010);
            matrices.pop();
        }
    }

    public static class PathLine implements Renderable {
        private final Vec3d[] blocks;
        private final int color;

        public PathLine(List<BlockPos> blocks, int color) {
            this.blocks = blocks.stream().map(Vec3d::ofCenter).toArray(Vec3d[]::new);
            this.color = color;
        }

        @Override
        public void render(WorldRenderContext context) {
            RenderSystem.assertThread(RenderSystem::isOnGameThread);

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

                    glRenderLines(new double[][]{{fromX, fromY, fromZ}, {toX, toY, toZ}}, r, g, b, 1.0f);
                }

                prev = current;
            }
        }
    }

    private static void glRenderLines(double[][] vertexes, float r, float g, float b, float a) {
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
