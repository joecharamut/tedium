package rocks.spaghetti.tedium.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.events.PauseMenuCallback;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(boolean showMenu, CallbackInfo info) {
        PauseMenuCallback.EVENT.invoker().onOpen();
    }
}
