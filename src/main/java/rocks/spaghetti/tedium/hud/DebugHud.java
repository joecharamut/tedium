package rocks.spaghetti.tedium.hud;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.util.Formatting;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.hud.components.HudComponent;
import rocks.spaghetti.tedium.hud.components.TextGridComponent;

import java.util.ArrayList;

public class DebugHud implements Hud {
    private final ArrayList<HudComponent> components = new ArrayList<>();
    
    private void refreshComponents() {
        components.clear();

        TextGridComponent textGrid = new TextGridComponent();
        textGrid.upperLeft("Tedium v0.0.1", Formatting.LIGHT_PURPLE);

        FakePlayer fakePlayer;
        if ((fakePlayer = FakePlayer.get()) != null && !fakePlayer.isAiDisabled()) {
            textGrid.upperLeft("State: AI", Formatting.GOLD);
            textGrid.upperLeft("");
            textGrid.upperLeft("Goals:");

            for (PrioritizedGoal goal : fakePlayer.getGoals()) {
                Formatting status;
                if (fakePlayer.getRunningGoals().contains(goal)) {
                    status = Formatting.GREEN;
                } else if (goal.canStart()) {
                    status = Formatting.YELLOW;
                } else {
                    status = Formatting.RED;
                }

                textGrid.upperLeft(String.format("%s (%s)", goal.getGoal(), goal.getPriority()), status);
            }
        } else {
            textGrid.upperLeft("State: Player", Formatting.GREEN);
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
