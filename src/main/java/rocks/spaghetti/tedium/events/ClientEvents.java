package rocks.spaghetti.tedium.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ClientEvents {
    Event<ClientJoinWorldEvent> JOIN_WORLD = EventFactory.createArrayBacked(ClientJoinWorldEvent.class,
            listeners -> () -> {
                for (ClientJoinWorldEvent listener : listeners) {
                    listener.onJoin();
                }
            });

    Event<ClientDisconnectEvent> DISCONNECT = EventFactory.createArrayBacked(ClientDisconnectEvent.class,
            listeners -> () -> {
                for (ClientDisconnectEvent listener : listeners) {
                    listener.onDisconnect();
                }
            });

    interface ClientJoinWorldEvent {
        void onJoin();
    }

    interface ClientDisconnectEvent {
        void onDisconnect();
    }
}
