package rocks.spaghetti.tedium.hud;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.hud.components.HudComponent;
import rocks.spaghetti.tedium.hud.components.TextComponent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class DebugHud implements Hud {
    private final ArrayList<HudComponent> components = new ArrayList<>();
    
    private void refreshComponents() {
        components.clear();
        int lineHeight = TextComponent.getLineHeight();
        int y = 0;

        components.add(new TextComponent("Tedium v0.0.1", 0, y).setColor(Color.MAGENTA));
        y += lineHeight;

        FakePlayer fakePlayer;
        if ((fakePlayer = FakePlayer.get()) != null && !fakePlayer.isAiDisabled()) {
            components.add(new TextComponent("State: AI", 0, y).setColor(Color.YELLOW));
            y += lineHeight;

            y += lineHeight;
            components.add(new TextComponent("Goals:", 0, y).setColor(Color.WHITE));
            y += lineHeight;
            List<PrioritizedGoal> runningGoals = fakePlayer.getRunningGoals();
            for (PrioritizedGoal goal : fakePlayer.getGoals()) {
                components.add(new TextComponent(String.format("%s (%s)", goal.getGoal(), goal.getPriority()), 0, y)
                        .setColor(runningGoals.contains(goal) ? Color.GREEN : Color.YELLOW));
                y += lineHeight;
            }
        } else {
            components.add(new TextComponent("State: Player", 0, y).setColor(Color.GREEN));
            y += lineHeight;
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
