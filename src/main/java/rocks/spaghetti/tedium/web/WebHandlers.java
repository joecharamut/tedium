package rocks.spaghetti.tedium.web;

import net.minecraft.client.MinecraftClient;
import rocks.spaghetti.tedium.ClientEntrypoint;
import rocks.spaghetti.tedium.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WebHandlers {
    public void registerWebContexts(WebServer webServer) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        assert client.world != null;

        webServer.registerPath("/screenshot", session -> {
            byte[] imageBytes = { };
            try {
                imageBytes = ClientEntrypoint.takeScreenshotBlocking().getBytes();
            } catch (IOException e) { Log.catching(e); }

            return WebServer.bytesResponse(imageBytes, "image/png");
        });


        webServer.registerPath("/name", (WebServer.StringRequestHandler) () -> client.player.getDisplayName().asString());
        webServer.registerPath("/position", (WebServer.StringRequestHandler) () -> client.player.getPos().toString());
        webServer.registerPath("/under", (WebServer.StringRequestHandler) () -> client.world.getBlockState(client.player.getBlockPos().down()).toString());

        webServer.registerPath("/look", session -> {
            Map<String, List<String>> query = session.getParameters();

            float yaw = 0;
            if (query.containsKey("yaw")) {
                yaw = Float.parseFloat(query.get("yaw").get(0));
            }

            client.player.yaw = yaw;
            return WebServer.stringResponse("OK");
        });
    }
}
