package rocks.spaghetti.tedium.hud;

import net.minecraft.client.util.math.MatrixStack;

public interface Hud {
    void render(MatrixStack matrixStack, float tickDelta);
}
