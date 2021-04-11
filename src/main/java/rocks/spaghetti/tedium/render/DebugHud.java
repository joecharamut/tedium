package rocks.spaghetti.tedium.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import rocks.spaghetti.tedium.mixin.MinecraftClientAccessor;
import rocks.spaghetti.tedium.render.hud.HudComponent;
import rocks.spaghetti.tedium.render.hud.TextGridComponent;
import rocks.spaghetti.tedium.util.Minecraft;

import java.util.ArrayList;

public class DebugHud {
    private static final ArrayList<HudComponent> components = new ArrayList<>();
    private static boolean enabled = false;
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final GameOptions options = ((MinecraftClientAccessor) MinecraftClient.getInstance()).getGameOptions();

    private DebugHud() {

    }

    public static void setEnabled(boolean state) {
        enabled = state;
    }

    public static void toggleEnabled() {
        setEnabled(!enabled);
    }

    public static void render(MatrixStack matrixStack, float tickDelta) {
        if (!enabled) return;
        // dont draw over/under debug menu or player list
        if (options.debugEnabled) return;
        if (!client.isInSingleplayer() && options.keyPlayerList.isPressed()) return;

        refreshComponents();
        for (HudComponent component : components) {
            component.render(matrixStack, tickDelta);
        }
    }

    private static void refreshComponents() {
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
}
