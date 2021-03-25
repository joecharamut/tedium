package rocks.spaghetti.tedium.core;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class PlayerInventoryHelper {
    private final MinecraftClient client;
    private final FakePlayer player;

    public PlayerInventoryHelper(FakePlayer player) {
        this.client = MinecraftClient.getInstance();
        this.player = player;
    }

    public static int getToolSlotFor(FakePlayer player, BlockState block) {
        if (!block.isToolRequired()) return -1;

        PlayerInventory inventory = player.getRealPlayer().inventory;
        return IntStream.range(0, inventory.main.size() - 1)
                .mapToObj(i -> new Pair<>(i, inventory.main.get(i)))
                .filter(pair -> pair.getRight().isEffectiveOn(block))
                .sorted(Comparator.comparingInt(pair -> pair.getRight().getDamage()))
                .map(Pair::getLeft)
                .findFirst().orElse(-1);
    }

    public Map<Item, Integer> getItemCounts() {
        // todo damage values all counted as same item
        PlayerInventory inventory = player.getRealPlayer().inventory;
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
        PlayerInventory inventory = player.getRealPlayer().inventory;
        ArrayList<Integer> emptySlots = new ArrayList<>();

        for (int i = 0; i < inventory.main.size(); ++i) {
            if (inventory.main.get(i).isEmpty()) {
                emptySlots.add(i);
            }
        }

        return emptySlots.toArray(new Integer[0]);
    }
}
