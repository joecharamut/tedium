package rocks.spaghetti.tedium.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import rocks.spaghetti.tedium.render.hud.HudComponent;
import rocks.spaghetti.tedium.render.hud.TextGridComponent;
import rocks.spaghetti.tedium.util.Minecraft;

import java.util.ArrayList;

public class DebugHud implements Hud {
    private final ArrayList<HudComponent> components = new ArrayList<>();
    
    private void refreshComponents() {
        components.clear();

        TextGridComponent textGrid = new TextGridComponent();
        textGrid.upperLeft("Tedium Debug Info", Formatting.LIGHT_PURPLE);

        boolean aiControl = Minecraft.isInputDisabled();

        if (aiControl) {
            textGrid.upperLeft("Control State: AI", Formatting.GOLD);
        } else {
            textGrid.upperLeft("Control State: Player", Formatting.GREEN);
        }

        if (MinecraftClient.getInstance().player != null) {
            Vec3d playerPos = MinecraftClient.getInstance().player.getPos();
            textGrid.upperLeft(String.format("Position: %.2f %.2f %.2f", playerPos.x, playerPos.y, playerPos.z));
        }

        if (aiControl) {
            textGrid.upperLeft("");
            textGrid.upperLeft("Goals:");
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
