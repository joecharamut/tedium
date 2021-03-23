package rocks.spaghetti.tedium.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.core.FakePlayer;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    private boolean serverSide = false;
    private FakePlayer fakePlayer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(World world, BlockPos pos, float yaw, GameProfile profile, CallbackInfo info) {
        serverSide = !world.isClient;
        if (serverSide) return;

        //noinspection ConstantConditions
        fakePlayer = FakePlayer.create((ClientPlayerEntity) (Object) this);
    }

    @Inject(method = "tickNewAi", at = @At("HEAD"))
    private void tickNewAi(CallbackInfo info) {
        if (serverSide) return;
        fakePlayer.tickNewAi();
    }
}
