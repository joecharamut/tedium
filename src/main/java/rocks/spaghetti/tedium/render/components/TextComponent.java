package rocks.spaghetti.tedium.render.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;


public class TextComponent implements HudComponent {
    private final BaseText text;
    private final int x;
    private final int y;
    private Formatting formatting = Formatting.WHITE;
    private boolean shadow = true;

    public TextComponent(BaseText text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public TextComponent(String text, int x, int y) {
        this(new LiteralText(text), x, y);
    }

    public TextComponent setFormatting(Formatting formatting) {
        this.formatting = formatting;
        return this;
    }

    public TextComponent setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public int width() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return textRenderer.getWidth(text);
    }

    public static int getLineHeight() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return textRenderer.fontHeight;
    }

    public static int getStringWidth(String str) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return textRenderer.getWidth(str);
    }

    @Override
    public void render(MatrixStack matrixStack, float tickDelta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        if (shadow) {
            textRenderer.drawWithShadow(matrixStack, text.formatted(formatting), x, y, formatting.getColorValue());
        } else {
            textRenderer.draw(matrixStack, text.formatted(formatting), x, y, formatting.getColorValue());
        }
    }
}
