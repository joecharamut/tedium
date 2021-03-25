package rocks.spaghetti.tedium.crafting;

import net.minecraft.item.Item;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Recipes {
    private Recipes() { throw new IllegalStateException("Utility Class"); }

    private static final Map<Item, CraftingRecipe> recipesByOutput = new HashMap<>();

    public static CraftingRecipe CRAFTING_TABLE;

    public static void initRecipes(World world) {
        RecipeManager manager = world.getRecipeManager();
        Optional<? extends Recipe<?>> temp;

        temp = manager.get(new Identifier("minecraft", "crafting_table"));
        if (temp.isPresent() && temp.get() instanceof CraftingRecipe) CRAFTING_TABLE = (CraftingRecipe) temp.get();

        for (CraftingRecipe recipe : world.getRecipeManager().listAllOfType(RecipeType.CRAFTING)) {
            recipesByOutput.put(recipe.getOutput().getItem(), recipe);
        }
    }

    public static CraftingRecipe getRecipeFor(Item item) {
        return recipesByOutput.getOrDefault(item, null);
    }

    public static Identifier getTagFor(Tag<Item> tag) {
        return ServerTagManagerHolder.getTagManager().getItems().getTagId(tag);
    }
}
