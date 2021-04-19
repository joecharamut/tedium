package rocks.spaghetti.tedium.mixin;

import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Ingredient.TagEntry.class)
public interface IngredientTagEntryAccessor {
    @Accessor("tag")
    Tag<Item> getTag();
}
