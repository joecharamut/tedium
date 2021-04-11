package rocks.spaghetti.tedium.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface MouseEvents {
    Event<LockCursorCallback> LOCK_EVENT = EventFactory.createArrayBacked(LockCursorCallback.class,
            listeners -> () -> {
                for (LockCursorCallback listener : listeners) {
                    ActionResult result = listener.onLock();
                    if (result != ActionResult.PASS) return result;
                }
                return ActionResult.PASS;
            });

    Event<MouseButtonCallback> BUTTON_EVENT = EventFactory.createArrayBacked(MouseButtonCallback.class,
            listeners -> (window, button, action, mods) -> {
                for (MouseButtonCallback listener : listeners) {
                    ActionResult result = listener.onButton(window, button, action, mods);
                    if (result != ActionResult.PASS) return result;
                }
                return ActionResult.PASS;
            });

    Event<MouseScrollCallback> SCROLL_EVENT = EventFactory.createArrayBacked(MouseScrollCallback.class,
            listeners -> (window, horizontal, vertical) -> {
                for (MouseScrollCallback listener : listeners) {
                    ActionResult result = listener.onScroll(window, horizontal, vertical);
                    if (result != ActionResult.PASS) return result;
                }
                return ActionResult.PASS;
            });


    interface LockCursorCallback {
        ActionResult onLock();
    }

    interface MouseButtonCallback {
        ActionResult onButton(long window, int button, int action, int mods);
    }

    interface MouseScrollCallback {
        ActionResult onScroll(long window, double horizontal, double vertical);
    }
}
