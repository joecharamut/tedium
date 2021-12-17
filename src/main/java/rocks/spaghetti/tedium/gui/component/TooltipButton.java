package rocks.spaghetti.tedium.gui.component;

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WButton;
import net.minecraft.text.Text;

public class TooltipButton extends WButton {
    private Text tooltipText;

    public TooltipButton setTooltip(Text tooltip) {
        this.tooltipText = tooltip;
        return this;
    }

    @Override
    public void addTooltip(TooltipBuilder tooltip) {
        if (tooltipText != null) {
            tooltip.add(tooltipText);
        }
        super.addTooltip(tooltip);
    }
}
