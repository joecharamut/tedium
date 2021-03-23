package rocks.spaghetti.tedium.interaction.action;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InventoryCraftingAction implements ClientAction {
    private final CraftingRecipe recipe;
    private final int count;

    private int ticks = 0;

    public InventoryCraftingAction(CraftingRecipe recipe, int count) {
        this.recipe = recipe;
        this.count = count;

        if (!recipe.fits(2, 2)) {
            throw new IllegalArgumentException("Recipe must be a 2x2 recipe");
        }

        // if has items
        if (!checkItems()) {
            // todo maybe return which are missing
            throw new InsufficientIngredientException();
        }
    }

    public InventoryCraftingAction(CraftingRecipe recipe) {
        this(recipe, 1);
    }

    @Override
    public boolean done() {
        return ticks == 2;
    }

    private boolean checkItems() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        PlayerInventory inventory = player.inventory;

        final boolean[] enoughItems = {true};
        recipe.getPreviewInputs().stream()
                .map(Ingredient::getMatchingStacksClient)
                .map(Arrays::asList)
                .map(stacks -> stacks.stream()
                        .map(player.inventory::getSlotWithStack)
                        .filter(slotId -> slotId != -1)
                        .findFirst()
                        .orElse(-1))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((slot, count) -> {
                    if (slot == -1) {
                        enoughItems[0] = false;
                        return;
                    }

                    if (inventory.getStack(slot).getCount() < count) {
                        enoughItems[0] = false;
                    }
                });

        return enoughItems[0];
    }

    @Override
    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        assert player != null;

        int syncId = player.playerScreenHandler.syncId;
        if (ticks == 0) {
            client.interactionManager.clickRecipe(syncId, recipe, false);
        } else if (ticks == 1) {
            final int[] finalSlotId = {-1};
            player.playerScreenHandler.slots
                    .stream()
                    .filter(slot -> slot instanceof CraftingResultSlot)
                    .findFirst()
                    .ifPresent(slot -> finalSlotId[0] = slot.id);
            int resultSlotId = finalSlotId[0];

            client.interactionManager.clickSlot(syncId, resultSlotId, 0, SlotActionType.QUICK_MOVE, player);
        }

        ticks++;
    }

    public static class InsufficientIngredientException extends RuntimeException { }
}
