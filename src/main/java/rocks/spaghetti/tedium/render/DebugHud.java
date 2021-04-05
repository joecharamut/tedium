package rocks.spaghetti.tedium.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.graalvm.polyglot.Source;
import rocks.spaghetti.tedium.core.FakePlayer;
import rocks.spaghetti.tedium.render.hud.HudComponent;
import rocks.spaghetti.tedium.render.hud.TextGridComponent;
import rocks.spaghetti.tedium.script.ScriptEnvironment;

import java.util.ArrayList;

public class DebugHud implements Hud {
    private final ArrayList<HudComponent> components = new ArrayList<>();
    
    private void refreshComponents() {
        components.clear();

        TextGridComponent textGrid = new TextGridComponent();
        textGrid.upperLeft("Tedium Debug Info", Formatting.LIGHT_PURPLE);

        FakePlayer fakePlayer = FakePlayer.get();
        boolean aiControl = fakePlayer != null && !fakePlayer.isAiDisabled();

        Source source = ScriptEnvironment.getInstance().getRunningSource();

        if (aiControl) {
            textGrid.upperLeft("Control State: AI", Formatting.GOLD);
        } else {
            textGrid.upperLeft("Control State: Player", Formatting.GREEN);
        }

        if (source != null) {
            textGrid.upperLeft(String.format("Script State: Running (%s)", source.getName()), Formatting.GREEN);
        } else {
            textGrid.upperLeft("Script State: Idle", Formatting.YELLOW);
        }

        if (MinecraftClient.getInstance().player != null) {
            Vec3d playerPos = MinecraftClient.getInstance().player.getPos();
            textGrid.upperLeft(String.format("Position: %.2f %.2f %.2f", playerPos.x, playerPos.y, playerPos.z));
        }

        if (aiControl) {
            textGrid.upperLeft("");
            textGrid.upperLeft("Goals:");

            for (PrioritizedGoal goal : fakePlayer.getGoals()) {
                Formatting status;
                if (fakePlayer.getRunningGoals().contains(goal)) {
                    status = Formatting.GREEN;
                } else {
                    status = Formatting.RED;
                }

                textGrid.upperLeft(String.format("%s (%s)", goal.getGoal(), goal.getPriority()), status);
            }
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
