package rocks.spaghetti.tedium.core.ai;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import rocks.spaghetti.tedium.Log;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.core.InteractionManager;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class EatFoodGoal extends Goal {
    private final FakePlayer entity;
    private final ClientPlayerInteractionManager interactionManager;
    private int maxTicks = 0;
    private int ticks = 0;

    public EatFoodGoal(FakePlayer entity) {
        this.entity = entity;
        this.interactionManager = MinecraftClient.getInstance().interactionManager;
    }

    private int getBestFoodSlot() {
        List<ItemStack> inventory = this.entity.getInventory().main;
        return IntStream.range(0, inventory.size())
                .mapToObj(i -> new Pair<>(i, inventory.get(i).getItem()))
                .filter(pair -> pair.getSecond().getFoodComponent() != null)
                .sorted(Comparator.comparingInt(pair -> pair.getSecond().getFoodComponent().getHunger()))
                .map(Pair::getFirst)
                .findFirst().orElse(-1);
    }

    @Override
    public boolean canStart() {
        return entity.getHungerLevel() < 20 && getBestFoodSlot() != -1;
    }

    @Override
    public boolean shouldContinue() {
        return ticks < maxTicks;
    }

    @Override
    public void start() {
        int theSlot = getBestFoodSlot();
        InteractionManager.pushEvent(new InteractionManager.InventoryPickEvent(theSlot));

        ItemStack food = entity.getInventory().getStack(theSlot);
        maxTicks = food.getItem().getMaxUseTime(food);
        ticks = 1;
        MinecraftClient.getInstance().options.keyUse.setPressed(true);
        interactionManager.interactItem(entity.getRealPlayer(), entity.world, Hand.MAIN_HAND);
    }

    @Override
    public void stop() {
        MinecraftClient.getInstance().options.keyUse.setPressed(false);
    }

    @Override
    public void tick() {
        ticks++;
        MinecraftClient.getInstance().options.keyUse.setPressed(true);
    }
}
