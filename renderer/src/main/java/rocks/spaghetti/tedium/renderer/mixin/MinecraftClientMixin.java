package rocks.spaghetti.tedium.renderer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.util.WindowProvider;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.renderer.WindowProviderProxy;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Final @Mutable private WindowProvider windowProvider;

    @Inject(method = "<init>", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/MinecraftClient;windowProvider:Lnet/minecraft/client/util/WindowProvider;",
            opcode = Opcodes.GETFIELD,
            ordinal = 0,
            args = "log=false"
    ))
    private void onAssignWindowProvider(RunArgs args, CallbackInfo ci) {
        System.err.println("onAssignWindowProvider()");
        this.windowProvider = WindowProviderProxy.createProxy();
    }
}
