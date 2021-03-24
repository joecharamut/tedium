package rocks.spaghetti.tedium.hud;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.hud.components.HudComponent;
import rocks.spaghetti.tedium.hud.components.TextGridComponent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class DebugHud implements Hud {
    private final ArrayList<HudComponent> components = new ArrayList<>();
    
    private void refreshComponents() {
        components.clear();

        TextGridComponent textGrid = new TextGridComponent();
        textGrid.upperLeft("Tedium v0.0.1", Color.MAGENTA);

        FakePlayer fakePlayer;
        if ((fakePlayer = FakePlayer.get()) != null && !fakePlayer.isAiDisabled()) {
            textGrid.upperLeft("State: AI", Color.YELLOW);
            textGrid.upperLeft("");
            textGrid.upperLeft("Goals:");

            List<PrioritizedGoal> runningGoals = fakePlayer.getRunningGoals();
            for (PrioritizedGoal goal : fakePlayer.getGoals()) {
                textGrid.upperLeft(String.format("%s (%s)", goal.getGoal(), goal.getPriority()),
                        runningGoals.contains(goal) ? Color.GREEN : Color.YELLOW);
            }
        } else {
            textGrid.upperLeft("State: Player", Color.GREEN);
        }

        components.add(textGrid);
    }

    @Override
    public void render(MatrixStack matrixStack, float tickDelta) {
        refreshComponents();
        for (HudComponent component : components) {
            component.render(matrixStack, tickDelta);
        }
    }
}
