package rocks.spaghetti.tedium.render.renderable;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

@FunctionalInterface
public interface Renderable {
    void render(WorldRenderContext context);
}
