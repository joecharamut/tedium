package rocks.spaghetti.tedium.core;


import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AbstractInventory {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ScreenHandler screen;

    private final List<ItemStack> allStacks;
    private final List<ItemStack> inventoryStacks;
    private final List<ItemStack> externalStacks;

    private final List<Slot> allSlots;
    private final List<Slot> inventorySlots;
    private final List<Slot> externalSlots;

    public AbstractInventory(List<ItemStack> eventStacks, ScreenHandler screen) {
        if (eventStacks.size() < 36) throw new IllegalArgumentException("Not enough slots");
        this.screen = screen;

        allStacks = eventStacks;
        externalStacks = eventStacks.subList(0, eventStacks.size() - 36);
        inventoryStacks = eventStacks.subList(eventStacks.size() - 36, eventStacks.size());

        allSlots = IntStream
                .range(0, eventStacks.size())
                .mapToObj(screen::getSlot)
                .collect(Collectors.toList());
        externalSlots = IntStream
                .range(0, eventStacks.size() - 36)
                .mapToObj(screen::getSlot)
                .collect(Collectors.toList());
        inventorySlots = IntStream
                .range(eventStacks.size() - 36, eventStacks.size())
                .mapToObj(screen::getSlot)
                .collect(Collectors.toList());
    }

    public void swapStacks(int from, int to) {
        if (client.interactionManager == null) return;
        if (from < 0 || from >= allSlots.size() || to < 0 || to >= allSlots.size()) return;

        client.interactionManager.clickSlot(screen.syncId, from, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(screen.syncId, to, 0, SlotActionType.PICKUP, client.player);
        client.interactionManager.clickSlot(screen.syncId, from, 0, SlotActionType.PICKUP, client.player);
    }

    public void quickMoveStack(int from) {
        if (client.interactionManager == null) return;
        if (from < 0 || from >= allSlots.size()) return;

        client.interactionManager.clickSlot(screen.syncId, from, 0, SlotActionType.QUICK_MOVE, client.player);
    }

    public List<ItemStack> getExternalStacks() {
        return externalStacks;
    }

    public void close() {
        if (client.currentScreen != null) {
            client.currentScreen.keyPressed(GLFW.GLFW_KEY_ESCAPE, -1, -1);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "{" +
                "allStacks=" + allStacks +
                ", inventoryStacks=" + inventoryStacks +
                ", externalStacks=" + externalStacks +
                "}";
    }
}
