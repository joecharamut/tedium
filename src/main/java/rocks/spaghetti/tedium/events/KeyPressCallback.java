package rocks.spaghetti.tedium.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;

public interface KeyPressCallback {
    Event<KeyPressCallback> EVENT = EventFactory.createArrayBacked(KeyPressCallback.class,
            listeners -> key -> {
                for (KeyPressCallback listener : listeners) {
                    ActionResult result = listener.keyPress(key);
                    if (result != ActionResult.PASS) return result;
                }
                return ActionResult.PASS;
            });

    ActionResult keyPress(InputUtil.Key key);
}
