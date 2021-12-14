package rocks.spaghetti.tedium.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
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

    public static void glRenderLines(double[][] vertexes, float r, float g, float b, float a) {
        RenderSystem.assertOnGameThread();
        GL11.glBegin(GL11.GL_LINES);

        GL11.glColor4f(r, g, b, a);
        for (double[] coords : vertexes) {
            GL11.glVertex3dv(coords);
        }

        GL11.glEnd();
    }

    public static void glRenderPolygon(double[][] vertexes, float r, float g, float b, float a) {
        RenderSystem.assertOnGameThread();
        GL11.glBegin(GL11.GL_POLYGON);

        GL11.glColor4f(r, g, b, a);
        for (double[] coords : vertexes) {
            GL11.glVertex3dv(coords);
        }

        GL11.glEnd();
    }
}
