package rocks.spaghetti.tedium.crafting;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import rocks.spaghetti.tedium.util.Log;
import rocks.spaghetti.tedium.mixin.IngredientMixin;
import rocks.spaghetti.tedium.mixin.IngredientTagEntryMixin;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeDataManager {
    private RecipeDataManager() { throw new IllegalStateException("Utility Class"); }

    private static final Map<Item, CraftingRecipe> recipesByOutput = new HashMap<>();
    private static final Map<CraftingRecipe, Map<String, Integer>> recipeIngredients = new HashMap<>();

    public static CraftingRecipe CRAFTING_TABLE;

    public static void init(RecipeManager manager) {
        initRecipes(manager);
    }

    private static void initRecipes(RecipeManager manager) {
        long startTime = System.currentTimeMillis();
        Log.info("Loading recipes...");

        recipesByOutput.clear();
        recipeIngredients.clear();

        Optional<? extends Recipe<?>> temp = manager.get(new Identifier("minecraft", "crafting_table"));
        if (temp.isPresent() && temp.get() instanceof CraftingRecipe) CRAFTING_TABLE = (CraftingRecipe) temp.get();

        for (CraftingRecipe recipe : manager.listAllOfType(RecipeType.CRAFTING)) {
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
                            .map(RecipeDataManager::toItemStack)
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
                                Arrays.stream(entries).map(RecipeDataManager::toItemStack).collect(Collectors.toList()));
                        ingredientsOk = false;
                        break;
                    }
                }
            }

            if (ingredientsOk) {
                recipeIngredients.put(recipe, ingredientMap);
            }
        }

        Log.info("Loaded {} Recipes ({} ms)", recipesByOutput.size(), System.currentTimeMillis() - startTime);
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
