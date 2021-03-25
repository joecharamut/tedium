package rocks.spaghetti.tedium.mixin;

import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Ingredient.class)
public interface IngredientMixin {
    @Accessor("entries")
    Ingredient.Entry[] getEntries();
}
