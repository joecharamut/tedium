package rocks.spaghetti.tedium.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.TriState;

public interface PlayerSprintCallback {
    Event<PlayerSprintCallback> EVENT = EventFactory.createArrayBacked(PlayerSprintCallback.class,
            listeners -> () -> {
                for (PlayerSprintCallback listener : listeners) {
                    TriState result = listener.onChange();
                    if (result != TriState.DEFAULT) {
                        return result;
                    }
                }

                return TriState.DEFAULT;
            });

    TriState onChange();
}
