package rocks.spaghetti.tedium.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.Log;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        //Log.info("Recv: {}", packet);
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"))
    private void sendImmediately(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo ci) {
//        if (Thread.currentThread().getName().equals("Server thread")) return;
//        Log.info("Pkt: {}", packet.getClass().getName());
    }
}
