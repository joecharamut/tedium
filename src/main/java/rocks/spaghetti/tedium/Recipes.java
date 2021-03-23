package rocks.spaghetti.tedium;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

public class Recipes {
    private Recipes() { throw new IllegalStateException("Utility Class"); }

    public static CraftingRecipe CRAFTING_TABLE;

    public static void initRecipes(World world) {
        RecipeManager manager = world.getRecipeManager();
        Optional<? extends Recipe<?>> temp;

        temp = manager.get(new Identifier("minecraft", "crafting_table"));
        if (temp.isPresent() && temp.get() instanceof CraftingRecipe) CRAFTING_TABLE = (CraftingRecipe) temp.get();

//        for (Recipe<CraftingInventory> recipe : world.getRecipeManager().listAllOfType(RecipeType.CRAFTING)) {
//            Log.info("Recipe {}: {}", recipe.getId(), recipe.getOutput());
//        }
    }
}
