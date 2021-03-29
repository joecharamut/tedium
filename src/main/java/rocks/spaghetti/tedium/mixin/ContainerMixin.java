package rocks.spaghetti.tedium.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.spaghetti.tedium.ClientEntrypoint;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.core.AbstractInventory;

import java.util.List;

@Mixin(ScreenHandler.class)
public abstract class ContainerMixin {
    private Class<?> current;

    @Inject(method = "updateSlotStacks", at = @At("HEAD"))
    public void onUpdateSlotStacks(List<ItemStack> stacks, CallbackInfo info) {
        if (this.getClass().equals(PlayerScreenHandler.class)) return;

        current = this.getClass();
        Log.info("cls: {}", current);
        if (stacks.size() < 36) return;

        AbstractInventory inv = new AbstractInventory(stacks, (ScreenHandler) (Object) this);
        Log.info("ab: {}", inv);
        ClientEntrypoint.setOpenContainer(inv);
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void onClose(PlayerEntity player, CallbackInfo info) {
        // workaround for single player integrated server
        if (Thread.currentThread().getName().equals("Server thread")) return;

        // player inventory opened isnt tracked because sure
        if (current == null) return;

        Log.info("closing: {}", current);
        ClientEntrypoint.setOpenContainer(null);
        current = null;
    }
}
