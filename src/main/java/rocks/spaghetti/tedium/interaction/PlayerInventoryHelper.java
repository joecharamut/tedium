package rocks.spaghetti.tedium.interaction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerInventoryHelper {
    private final MinecraftClient client;

    public PlayerInventoryHelper() {
        client = MinecraftClient.getInstance();
    }

    public void throwItem(ItemStack stack) {
        PlayerInventory inventory = client.player.inventory;
        inventory.getSlotWithStack(stack);
//        client.interactionManager.clickSlot();
    }

    public Map<Item, Integer> getItemCounts() {
        // todo damage values all counted as same item
        PlayerInventory inventory = client.player.inventory;
        Map<Item, Integer> items = new HashMap<>();

        for (ItemStack stack : inventory.main) {
            Item item = stack.getItem();
            int count = stack.getCount();

            if (items.containsKey(item)) {
                items.put(item, items.get(item) + count);
            } else {
                items.put(item, count);
            }
        }

        return items;
    }

    public Integer[] getEmptySlots() {
        PlayerInventory inventory = client.player.inventory;
        ArrayList<Integer> emptySlots = new ArrayList<>();

        for (int i = 0; i < inventory.main.size(); ++i) {
            if (inventory.main.get(i).isEmpty()) {
                emptySlots.add(i);
            }
        }

        return emptySlots.toArray(new Integer[0]);
    }
}
