package rocks.spaghetti.tedium.renderer.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void onSetup(long window, CallbackInfo ci) {
        ci.cancel();
    }
}
