package rocks.spaghetti.tedium.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PauseMenuCallback {
    Event<PauseMenuCallback> EVENT = EventFactory.createArrayBacked(PauseMenuCallback.class,
            listeners -> () -> {
                for (PauseMenuCallback listener : listeners) {
                    listener.onOpen();
                }
            });

    void onOpen();
}
