package rocks.spaghetti.tedium.mixin;

import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingMixin {
    @Invoker("reset")
    void invokeReset();

    @Accessor("keysById")
    static Map<String, KeyBinding> getKeysById() {
        throw new AssertionError();
    }
}
