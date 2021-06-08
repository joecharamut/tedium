package rocks.spaghetti.tedium.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.events.DeathCallback;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onDeathMessage", at = @At("HEAD"))
    private void onDeathMessage(DeathMessageS2CPacket packet, CallbackInfo ci) {
        if (client.player != null && packet.getEntityId() == client.player.getId()) {
            DeathCallback.EVENT.invoker().onDeath();
        }
    }
}
