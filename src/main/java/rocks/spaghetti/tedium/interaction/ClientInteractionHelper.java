package rocks.spaghetti.tedium.interaction;

import net.minecraft.client.MinecraftClient;
import rocks.spaghetti.tedium.interaction.action.ClientAction;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;


public class ClientInteractionHelper {
    private final Queue<ClientAction> eventQueue = new ArrayDeque<>();
    private ClientAction currentAction = null;

    public ClientInteractionHelper() {

    }

    public void disconnect() {
        eventQueue.clear();
    }

    public void enqueue(ClientAction action) {
        eventQueue.add(action);
    }

    public void enqueue(ClientAction[] actions) {
        Collections.addAll(eventQueue, actions);
    }

    public void tick(MinecraftClient client) {
        if (client.isPaused()) return;
        if (client.interactionManager == null) return;

        if (currentAction != null) {
            currentAction.tick(client);

            if (currentAction.done()) {
                currentAction = null;
            }
        } else {
            if (!eventQueue.isEmpty()) {
                currentAction = eventQueue.poll();
            }
        }
    }
}
