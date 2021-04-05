package rocks.spaghetti.tedium.render.hud;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

import java.util.LinkedList;
import java.util.List;

public class TextGridComponent implements HudComponent {
    private final List<TextComponent> upperLeftTexts = new LinkedList<>();
    private int upperLeftX = 2;
    private int upperLeftY = 2;

    public void upperLeft(String message, Formatting formatting, boolean shadow) {
        upperLeftTexts.add(new TextComponent(message, upperLeftX, upperLeftY).setFormatting(formatting).setShadow(shadow));
        upperLeftY += TextComponent.getLineHeight();
    }

    public void upperLeft(String message, Formatting formatting) {
        upperLeft(message, formatting, false);
    }

    public void upperLeft(String message) {
        upperLeft(message, Formatting.WHITE);
    }

    @Override
    public void render(MatrixStack matrixStack, float tickDelta) {
        for (int i = 0; i < upperLeftTexts.size(); i++) {
            TextComponent text = upperLeftTexts.get(i);
            if (text.width() == 0) continue;

            int height = TextComponent.getLineHeight();
            int width = text.width();
            int boxX = 2;
            int boxY = 2 + height * i;

            DrawableHelper.fill(matrixStack, boxX - 1, boxY - 1, boxX + width + 1, boxY + height - 1, 0x90505050);
            text.render(matrixStack, tickDelta);
        }
    }
}
