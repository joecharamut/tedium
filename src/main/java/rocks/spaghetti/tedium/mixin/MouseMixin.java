package rocks.spaghetti.tedium.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.ClientEntrypoint;
import rocks.spaghetti.tedium.events.MouseEvents;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    private void lockCursor(CallbackInfo info) {
        if (MouseEvents.LOCK_EVENT.invoker().onLock() == ActionResult.FAIL) {
            info.cancel();
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo info) {
        if (MouseEvents.BUTTON_EVENT.invoker().onButton(window, button, action, mods) == ActionResult.FAIL) {
            info.cancel();
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info) {
        if (MouseEvents.SCROLL_EVENT.invoker().onScroll(window, horizontal, vertical) == ActionResult.FAIL) {
            info.cancel();
        }
    }
}
