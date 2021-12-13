package rocks.spaghetti.tedium.renderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;

public class TediumRendererEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RendererAccess.INSTANCE.registerRenderer(new TestRenderer());


    }
}
