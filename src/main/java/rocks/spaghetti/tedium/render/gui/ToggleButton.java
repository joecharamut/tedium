package rocks.spaghetti.tedium.render.gui;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import rocks.spaghetti.tedium.util.Constants;

public class ToggleButton extends WButton {
    private static final Identifier NORMAL_BUTTON = new Identifier(Constants.MOD_ID, "textures/gui/button.png");
    private static final Identifier DISABLED_BUTTON = new Identifier(Constants.MOD_ID, "textures/gui/button_disabled.png");
    private static final Identifier HOVERED_BUTTON = new Identifier(Constants.MOD_ID, "textures/gui/button_hover.png");
    private static final Identifier SELECTED_BUTTON = new Identifier(Constants.MOD_ID, "textures/gui/button_select.png");
    private static final Identifier HOVERED_SELECTED_BUTTON = new Identifier(Constants.MOD_ID, "textures/gui/button_hover_select.png");

    private boolean toggled = false;

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    @Override
    public InputResult onClick(int x, int y, int button) {
        if (button == 0 && isEnabled() && isWithinBounds(x, y)) {
            toggled = !toggled;
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            if (getOnClick() != null) getOnClick().run();
            return InputResult.PROCESSED;
        }
        return InputResult.IGNORED;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());

        Identifier buttonImage = NORMAL_BUTTON;
        if (!isEnabled()) {
            buttonImage = DISABLED_BUTTON;
        } else if (hovered || isFocused()) {
            if (toggled) {
                buttonImage = HOVERED_SELECTED_BUTTON;
            } else {
                buttonImage = HOVERED_BUTTON;
            }
        } else if (toggled) {
            buttonImage = SELECTED_BUTTON;
        }

        float px = 1 / 200f;
        float py = 1 / 20f;
        int halfWidth = getWidth() / 2;
        if (halfWidth > 198) halfWidth = 198;

        ScreenDrawing.texturedRect(matrices,
                x, y,
                getWidth() / 2, 20,
                buttonImage,
                0, 0,
                halfWidth * px, 20 * py,
                0xFFFFFFFF);

        ScreenDrawing.texturedRect(matrices,
                x + (getWidth() / 2), y,
                getWidth() / 2, 20,
                buttonImage,
                (200 - (getWidth() / 2F)) * px, 0,
                200 * px, 20 * py,
                0xFFFFFFFF);

        if (getIcon() != null) {
            getIcon().paint(matrices, x + 2, y + 2, 16);
        }

        if (getLabel() != null) {
            int color = 0xE0E0E0;
            if (!isEnabled()) {
                color = 0xA0A0A0;
            } else if (toggled) {
                color = 0xFFFFA0;
            }

            int xOffset = (getIcon() != null && alignment == HorizontalAlignment.LEFT) ? 18 : 0;
            ScreenDrawing.drawStringWithShadow(matrices, getLabel().asOrderedText(), alignment, x + xOffset, y + ((20 - 8) / 2), width, color);
        }
    }
}
