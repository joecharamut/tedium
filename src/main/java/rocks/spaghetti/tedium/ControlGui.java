package rocks.spaghetti.tedium;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;
import rocks.spaghetti.tedium.script.ScriptEnvironment;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ControlGui extends LightweightGuiDescription {
    private static final int GRID = 18;
    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 7;
    private static final int WIDTH = GRID*GRID_WIDTH;
    private static final int HEIGHT = GRID*GRID_HEIGHT;

    private ControlGui() {
        WTabPanel root = new WTabPanel();
        try {
            // center the stupid panel
            Field tabHeightField = WTabPanel.class.getDeclaredField("TAB_HEIGHT");
            tabHeightField.setAccessible(true);
            int tabHeight = tabHeightField.getInt(null);

            Field panelPaddingField = WTabPanel.class.getDeclaredField("PANEL_PADDING");
            panelPaddingField.setAccessible(true);
            int panelPadding = panelPaddingField.getInt(null);

            Method addWidget = WTabPanel.class.getDeclaredMethod("add", WWidget.class, int.class, int.class);
            addWidget.setAccessible(true);
            WPlainPanel spacer = new WPlainPanel();
            addWidget.invoke(root, spacer, 0, HEIGHT + tabHeight + panelPadding);
            spacer.setSize(0, tabHeight + panelPadding);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.catching(e);
        }
        setRootPanel(root);

        addTab(createMainPanel(), new ItemIcon(Items.NETHER_STAR), new TranslatableText("gui.tedium.tabMain"));
        addTab(createLocationPanel(), new ItemIcon(Items.TARGET), new TranslatableText("gui.tedium.tabLocations"));
        addTab(new ScriptPanel(), new ItemIcon(Items.COMMAND_BLOCK), new TranslatableText("gui.tedium.tabScripts"));

        root.validate(this);
    }

    public static CottonClientScreen createScreen() {
        return new CottonClientScreen(new ControlGui()) {
            @Override
            public boolean isPauseScreen() {
                return false;
            }
        };
    }

    private void addTab(WPanel panel, Icon icon, Text tooltip) {
        panel.setSize(WIDTH, HEIGHT);
        ((WTabPanel) rootPanel).add(panel, tab -> {
            tab.icon(icon);
            tab.tooltip(tooltip);
        });
    }

    private WPanel createMainPanel() {
        WGridPanel panel = new WGridPanel();

        panel.add(new WText(new TranslatableText("gui.tedium.tabMain")), 0, 0, GRID_WIDTH, 1);

        WButton button = new WButton(new TranslatableText("gui.tedium.button"));
        panel.add(button, 0, 1, 4, 1);

        return panel;
    }

    private WPanel createLocationPanel() {
        WGridPanel panel = new WGridPanel();

        panel.add(new WText(new TranslatableText("gui.tedium.tabLocations")), 0, 0, GRID_WIDTH, 1);

        return panel;
    }

    @Override
    public void addPainters() {
        super.addPainters();
        this.rootPanel.setBackgroundPainter(null);
    }

    private static class ScriptPanel extends WPlainPanel {
        private final List<ToggleButton> scriptButtons = new ArrayList<>();
        private final WButton runButton = new WButton(new TextureIcon(new Identifier("tedium:textures/gui/play.png"))).setEnabled(false);
        private final WButton deleteButton = new WButton(new TextureIcon(new Identifier("tedium:textures/gui/trash.png"))).setEnabled(false);
        private ScriptFile selectedScript = null;

        public ScriptPanel() {
            add(new WText(new TranslatableText("gui.tedium.tabScripts")), 0, 0, WIDTH, GRID);
            add(runButton, 0, GRID, 20, 20);
            runButton.setOnClick(() -> {
                Screen screen = MinecraftClient.getInstance().currentScreen;
                if (screen != null) screen.keyPressed(GLFW.GLFW_KEY_ESCAPE, -1, -1);
                ClientEntrypoint.setFakePlayerState(true);
                ScriptEnvironment.getInstance().execFile(selectedScript.file);
            });
            // todo: delete button doesnt do anything yet
            // todo: refresh folder
            add(deleteButton, 0, 2*GRID+4, 20, 20);
            add(new WButton(new LiteralText("...")).setOnClick(() -> Util.openFile(ModData.getGlobalDir())), 0, HEIGHT, 20, 20);

            List<ScriptFile> listData = Arrays.stream(ModData.getGlobalFiles()).map(ScriptFile::new).collect(Collectors.toList());
            WListPanel<ScriptFile, ToggleButton> scriptList = new WListPanel<ScriptFile, ToggleButton>(listData, ToggleButton::new, (file, item) -> {
                item.setLabel(new LiteralText(file.name));
                item.setIcon(new ItemIcon(file.icon));
            }) {
                @Override
                public void layout() {
                    super.layout();
                    scriptButtons.clear();
                    configured.forEach((file, button) -> {
                        button.setOnClick(() -> onButtonClick(button, file));
                        scriptButtons.add(button);
                    });
                }
            };
            scriptList.setBackgroundPainter(BackgroundPainter.SLOT);
            add(scriptList, (int) (1.5 * GRID), GRID + 1, (GRID_WIDTH - 1) * GRID, HEIGHT);
        }

        private void onButtonClick(ToggleButton clicked, ScriptFile file) {
            runButton.setEnabled(true);
            deleteButton.setEnabled(true);
            selectedScript = file;
            scriptButtons.forEach(button -> button.toggled = (button == clicked));
        }

        private static class ScriptFile {
            private final String name;
            private final File file;
            private Item icon = Items.PAPER;

            public ScriptFile(File file) {
                this.name = file.getName();
                this.file = file;
                String content = Util.readFileToString(file);
                Pattern regex = Pattern.compile("//\\s*@(\\w+)\\s*(.*)");

                if (!content.isEmpty()) {
                    for (String line : content.split("\n")) {
                        Matcher matcher = regex.matcher(line);
                        if (matcher.matches()) {
                            String prop = matcher.group(1).toLowerCase();
                            String value = matcher.group(2).toLowerCase();
                            if (prop.equals("icon")) {
                                icon = Registry.ITEM.getOrEmpty(new Identifier(value)).orElse(Items.PAPER);
                            }
                        }
                    }
                }
            }
        }
    }

    private static class ToggleButton extends WButton {
        private static final Identifier NORMAL_BUTTON = new Identifier(Constants.MOD_ID, "textures/gui/button.png");
        private static final Identifier DISABLED_BUTTON = new Identifier(Constants.MOD_ID, "textures/gui/button_disabled.png");
        private static final Identifier HOVERED_BUTTON = new Identifier(Constants.MOD_ID, "textures/gui/button_hover.png");
        private static final Identifier SELECTED_BUTTON = new Identifier(Constants.MOD_ID, "textures/gui/button_select.png");
        private static final Identifier HOVERED_SELECTED_BUTTON = new Identifier(Constants.MOD_ID, "textures/gui/button_hover_select.png");

        private boolean toggled = false;

        @Override
        public void onClick(int x, int y, int button) {
            if (button == 0 && isEnabled() && isWithinBounds(x, y)) {
                toggled = !toggled;
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                if (getOnClick() != null) getOnClick().run();
            }
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

            ScreenDrawing.texturedRect(
                    x, y,
                    getWidth() / 2, 20,
                    buttonImage,
                    0, 0,
                    halfWidth * px, 20 * py,
                    0xFFFFFFFF);

            ScreenDrawing.texturedRect(
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
}
