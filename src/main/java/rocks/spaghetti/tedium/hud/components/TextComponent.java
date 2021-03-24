package rocks.spaghetti.tedium.hud.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;

import java.awt.*;

public class TextComponent implements HudComponent {
    private final BaseText text;
    private final int x;
    private final int y;
    private int color = 0xffffff;
    private boolean shadow = true;

    public TextComponent(BaseText text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public TextComponent(String text, int x, int y) {
        this(new LiteralText(text), x, y);
    }

    public TextComponent setColor(int color) {
        this.color = color;
        return this;
    }

    public TextComponent setColor(Color color) {
        this.color = color.getRGB();
        return this;
    }

    public TextComponent setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public static int getLineHeight() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return textRenderer.fontHeight;
    }

    @Override
    public void render(MatrixStack matrixStack, float tickDelta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (shadow) {
            textRenderer.drawWithShadow(matrixStack, text, x, y, color);
        } else {
            textRenderer.draw(matrixStack, text, x, y, color);
        }
    }
}
