package rocks.spaghetti.tedium.hud.components;

import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class TextGridComponent implements HudComponent {
    private final List<TextComponent> upperLeftTexts = new LinkedList<>();
    private int upperLeftX = 4;
    private int upperLeftY = 4;

    private final List<TextComponent> upperRightTexts = new LinkedList<>();
    private int upperRightX = 4;
    private int upperRightY = 4;

    public void upperLeft(String message, Color color, boolean shadow) {
        upperLeftTexts.add(new TextComponent(message, upperLeftX, upperLeftY).setColor(color).setShadow(shadow));
        upperLeftY += TextComponent.getLineHeight();
    }

    public void upperLeft(String message, Color color) {
        upperLeft(message, color, true);
    }

    public void upperLeft(String message) {
        upperLeft(message, Color.WHITE);
    }

    public void upperRight(String message, Color color, boolean shadow) {
        upperRightTexts.add(new TextComponent(message, upperRightX, upperRightY).setColor(color).setShadow(shadow));
        upperRightY += TextComponent.getLineHeight();
    }

    public void upperRight(String message, Color color) {
        upperRight(message, color, true);
    }

    public void upperRight(String message) {
        upperRight(message, Color.WHITE);
    }

    @Override
    public void render(MatrixStack matrixStack, float tickDelta) {
        upperLeftTexts.forEach(text -> text.render(matrixStack, tickDelta));
        upperRightTexts.forEach(text -> text.render(matrixStack, tickDelta));
    }
}
