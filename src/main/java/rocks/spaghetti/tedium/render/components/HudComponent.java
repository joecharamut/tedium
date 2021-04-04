package rocks.spaghetti.tedium.render.components;

import net.minecraft.client.util.math.MatrixStack;

public interface HudComponent {

    void render(MatrixStack matrixStack, float tickDelta);
}
