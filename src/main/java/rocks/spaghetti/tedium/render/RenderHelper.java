package rocks.spaghetti.tedium.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import rocks.spaghetti.tedium.render.renderable.FloatingText;
import rocks.spaghetti.tedium.render.renderable.Renderable;
import rocks.spaghetti.tedium.render.renderable.SchematicRenderer;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RenderHelper {
    private RenderHelper() { throw new IllegalStateException("Utility Class"); }


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
        RenderSystem.assertOnRenderThread();

        for (Renderable obj : objs) {
            if (obj instanceof FloatingText || obj instanceof SchematicRenderer) {
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
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        GL.createCapabilities();

        Renderable obj;
        while ((obj = beforeDebugQueue.poll()) != null) {
            obj.render(context);
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
    }

    @FunctionalInterface
    public interface RenderListener {
        void onRenderStart();
    }

    public static void drawLines(Tessellator tess, BufferBuilder buffer, double[][] points, float r, float g, float b, float a) {
        RenderSystem.assertOnGameThread();
        if (points.length % 2 != 0) {
            throw new IllegalArgumentException("Points must be a multiple of 2");
        }

        for (int i = 0; i < points.length; i += 2) {
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

            buffer.vertex(points[i][0], points[i][1], points[i][2]).color(r, g, b, a).next();
            buffer.vertex(points[i + 1][0], points[i + 1][1], points[i + 1][2]).color(r, g, b, a).next();

            tess.draw();
        }
    }

    public static void drawQuads(Tessellator tess, BufferBuilder buffer, double[][] points, float r, float g, float b, float a) {
        RenderSystem.assertOnGameThread();
        if (points.length % 4 != 0) {
            throw new IllegalArgumentException("Points must be a multiple of 4");
        }

        for (int i = 0; i < points.length; i += 4) {
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            buffer.vertex(points[i][0], points[i][1], points[i][2]).color(r, g, b, a).next();
            buffer.vertex(points[i + 1][0], points[i + 1][1], points[i + 1][2]).color(r, g, b, a).next();
            buffer.vertex(points[i + 2][0], points[i + 2][1], points[i + 2][2]).color(r, g, b, a).next();
            buffer.vertex(points[i + 3][0], points[i + 3][1], points[i + 3][2]).color(r, g, b, a).next();

            tess.draw();
        }


    }
}
