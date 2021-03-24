package rocks.spaghetti.tedium.mixin;

import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.ClientEntrypoint;


@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
    @Inject(method = "onKeyPressed", at = @At("HEAD"), cancellable = true)
    private static void onKeyPressed(InputUtil.Key key, CallbackInfo info) {
        if (ClientEntrypoint.isInputDisabled()) {
            for (KeyBinding bind : ClientEntrypoint.modKeybindings) {
                if (key.getTranslationKey().equals(bind.getBoundKeyTranslationKey())) {
                    return;
                }
            }

            info.cancel();
        }
    }
}
