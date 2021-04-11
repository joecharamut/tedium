package rocks.spaghetti.tedium.mixin;

import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.events.KeyPressCallback;


@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
    @Inject(method = "onKeyPressed", at = @At("HEAD"), cancellable = true)
    private static void onKeyPressed(InputUtil.Key key, CallbackInfo info) {
        if (KeyPressCallback.EVENT.invoker().keyPress(key) == ActionResult.FAIL) {
            info.cancel();
        }
    }
}
