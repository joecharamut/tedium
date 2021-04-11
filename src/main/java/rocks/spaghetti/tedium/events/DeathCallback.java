package rocks.spaghetti.tedium.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface DeathCallback {
    Event<DeathCallback> EVENT = EventFactory.createArrayBacked(DeathCallback.class,
            (listeners) -> () -> {
                for (DeathCallback listener : listeners) {
                    listener.onDeath();
                }
            });

    void onDeath();
}
