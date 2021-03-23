package rocks.spaghetti.tedium.interaction.action;

import net.minecraft.client.MinecraftClient;

public class CallbackAction implements ClientAction {
    private final Runnable callback;

    public CallbackAction(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public boolean done() {
        return true;
    }

    @Override
    public void tick(MinecraftClient client) {
        callback.run();
    }
}
