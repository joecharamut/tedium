package rocks.spaghetti.tedium.hud;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.hud.components.HudComponent;
import rocks.spaghetti.tedium.hud.components.TextComponent;

import java.awt.*;
import java.util.ArrayList;

public class DebugHud implements Hud {
    private final ArrayList<HudComponent> components = new ArrayList<>();
    
    public DebugHud() {
        refreshComponents();
    }

    private static final int LINE_HEIGHT = 16;
    private void refreshComponents() {
        components.clear();
        int y = 0;

        components.add(new TextComponent("Tedium v0.0.1", 0, y).setColor(Color.MAGENTA));
        y += LINE_HEIGHT;

        FakePlayer fakePlayer;
        if ((fakePlayer = FakePlayer.get()) != null && !fakePlayer.isAiDisabled()) {
            components.add(new TextComponent("State: AI", 0, y).setColor(Color.YELLOW));
            y += LINE_HEIGHT;

            components.add(new TextComponent("Tasks:", 0, y).setColor(Color.WHITE));
            y += LINE_HEIGHT;

            for (Task<?> task : fakePlayer.getBrain().getRunningTasks()) {
                components.add(new TextComponent(task.toString(), 0, y).setColor(Color.WHITE));
                y += LINE_HEIGHT;
            }

            y += LINE_HEIGHT;
            components.add(new TextComponent("Goals:", 0, y).setColor(Color.WHITE));
            y += LINE_HEIGHT;
            for (PrioritizedGoal goal : fakePlayer.getGoals()) {
                components.add(new TextComponent(goal.getGoal().toString(), 0, y).setColor(Color.WHITE));
                y += LINE_HEIGHT;
            }
        } else {
            components.add(new TextComponent("State: Player", 0, y).setColor(Color.GREEN));
            y += LINE_HEIGHT;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, float tickDelta) {
        refreshComponents();
        for (HudComponent component : components) {
            component.render(matrixStack, tickDelta);
        }
    }
}
