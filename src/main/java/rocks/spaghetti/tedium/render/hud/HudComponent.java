package rocks.spaghetti.tedium.render.hud;

import net.minecraft.client.util.math.MatrixStack;

public interface HudComponent {

    void render(MatrixStack matrixStack, float tickDelta);
}
