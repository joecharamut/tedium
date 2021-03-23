package rocks.spaghetti.tedium.hud.components;

import net.minecraft.client.util.math.MatrixStack;

public interface HudComponent {

    void render(MatrixStack matrixStack, float tickDelta);
}
