package rocks.spaghetti.tedium.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import rocks.spaghetti.tedium.events.PlayerSprintCallback;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @ModifyVariable(method = "setSprinting(Z)V", at = @At("HEAD"), ordinal = 0)
    private boolean onSetSprinting(boolean sprinting) {
        return PlayerSprintCallback.EVENT.invoker().onChange().orElse(sprinting);
    }
}
