package rocks.spaghetti.tedium.crafting;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.mixin.IngredientMixin;
import rocks.spaghetti.tedium.mixin.IngredientTagEntryMixin;

import java.util.*;
import java.util.stream.Collectors;

public class Recipes {
    private Recipes() { throw new IllegalStateException("Utility Class"); }

    private static final Map<Item, CraftingRecipe> recipesByOutput = new HashMap<>();
    private static final Map<CraftingRecipe, Map<String, Integer>> recipeIngredients = new HashMap<>();

    public static CraftingRecipe CRAFTING_TABLE;

    public static void initRecipes(final World world) {
        Thread recipeThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            Log.info("Loading recipes...");

            initRecipeThread(world);

            long endTime = System.currentTimeMillis();
            Log.info("Done ({} ms)", endTime - startTime);
        });
        recipeThread.start();
    }

    private static void initRecipeThread(final World world) {
        RecipeManager manager = world.getRecipeManager();
        Optional<? extends Recipe<?>> temp;

        temp = manager.get(new Identifier("minecraft", "crafting_table"));
        if (temp.isPresent() && temp.get() instanceof CraftingRecipe) CRAFTING_TABLE = (CraftingRecipe) temp.get();

        for (CraftingRecipe recipe : world.getRecipeManager().listAllOfType(RecipeType.CRAFTING)) {
            recipesByOutput.put(recipe.getOutput().getItem(), recipe);
            Map<String, Integer> ingredientMap = new HashMap<>();
            boolean ingredientsOk = true;

            for (Ingredient ingredient : recipe.getPreviewInputs()) {
                Ingredient.Entry[] entries = getEntriesFor(ingredient);
                if (entries.length == 0) continue;

                if (entries.length == 1) {
                    Ingredient.Entry entry = entries[0];
                    Class<?> entryClass = entry.getClass();

                    if (entryClass == Ingredient.TagEntry.class) {
                        String tag = getTagFor(toItemTag(entry)).toString();
                        int count = ingredientMap.getOrDefault(tag, 0);
                        ingredientMap.put(tag, count + 1);
                    } else if (entryClass == Ingredient.StackEntry.class) {
                        String item = Registry.ITEM.getId(toItemStack(entry).getItem()).toString();
                        int count = ingredientMap.getOrDefault(item, 0);
                        ingredientMap.put(item, count + 1);
                    } else {
                        Log.error("Unknown ingredient type: {}", entryClass);
                    }
                } else {
                    List<Identifier> itemTags = Arrays
                            .stream(entries)
                            .map(Recipes::toItemStack)
                            .map(ItemStack::getItem)
                            .map(item -> ItemTags.getTagGroup().getTagsFor(item))
                            .map(ids -> {
                                if (ids.isEmpty()) {
                                    return null;
                                } else if (ids.size() == 1) {
                                    return ids.toArray(new Identifier[0])[0];
                                } else {
                                    return ids.stream()
                                            .filter(id -> id.getNamespace().equals("minecraft"))
                                            .findFirst()
                                            .orElse(null);
                                }
                            })
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

                    if (itemTags.size() == 1) {
                        String tag = itemTags.get(0).toString();
                        int count = ingredientMap.getOrDefault(tag, 0);
                        ingredientMap.put(tag, count + 1);
                    } else {
                        // todo: fix this
                        Log.warn("Entries > 1: {} {}",
                                recipe.getId(),
                                Arrays.stream(entries).map(Recipes::toItemStack).collect(Collectors.toList()));
                        ingredientsOk = false;
                        break;
                    }
                }
            }

            if (ingredientsOk) {
                recipeIngredients.put(recipe, ingredientMap);
            }
        }
    }

    public static CraftingRecipe getRecipeFor(Item item) {
        return recipesByOutput.getOrDefault(item, null);
    }

    public static Map<String, Integer> getIngredientsFor(CraftingRecipe recipe) {
        if (recipe == null) return Collections.emptyMap();
        return recipeIngredients.getOrDefault(recipe, Collections.emptyMap());
    }

    public static Identifier getTagFor(Tag<Item> tag) {
        return ServerTagManagerHolder.getTagManager().getItems().getTagId(tag);
    }

    public static Ingredient.Entry[] getEntriesFor(Ingredient ingredient) {
        return ((IngredientMixin) ingredient).getEntries();
    }

    public static Tag<Item> toItemTag(Ingredient.Entry entry) {
        if (!(entry instanceof Ingredient.TagEntry)) {
            throw new IllegalArgumentException("Entry not instance of Ingredient.TagEntry");
        }

        return ((IngredientTagEntryMixin) entry).getTag();
    }

    public static ItemStack toItemStack(Ingredient.Entry entry) {
        if (!(entry instanceof Ingredient.StackEntry)) {
            throw new IllegalArgumentException("Entry not instance of Ingredient.StackEntry");
        }

        return entry.getStacks().stream().findFirst().orElse(ItemStack.EMPTY);
    }
}
