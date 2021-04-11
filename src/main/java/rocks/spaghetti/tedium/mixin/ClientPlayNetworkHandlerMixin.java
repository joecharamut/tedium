package rocks.spaghetti.tedium.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CombatEventS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.events.DeathCallback;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onCombatEvent", at = @At("HEAD"))
    private void onCombatEvent(CombatEventS2CPacket packet, CallbackInfo info) {
        if (packet.type == CombatEventS2CPacket.Type.ENTITY_DIED) {
            DeathCallback.EVENT.invoker().onDeath();
        }
    }
}
