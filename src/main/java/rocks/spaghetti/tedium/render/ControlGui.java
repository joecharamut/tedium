package rocks.spaghetti.tedium.render;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import io.github.cottonmc.cotton.gui.widget.icon.TextureIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;
import rocks.spaghetti.tedium.render.gui.ToggleButton;
import rocks.spaghetti.tedium.render.gui.TooltipButton;
import rocks.spaghetti.tedium.util.Constants;
import rocks.spaghetti.tedium.util.Log;
import rocks.spaghetti.tedium.util.ModData;
import rocks.spaghetti.tedium.util.Util;

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

        addTab(new MainPanel(), new ItemIcon(Items.NETHER_STAR), new TranslatableText("gui.tedium.tabMain"));
        addTab(new LocationPanel(), new ItemIcon(Items.COMPASS), new TranslatableText("gui.tedium.tabLocations"));
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

    @Override
    public void addPainters() {
        super.addPainters();
        this.rootPanel.setBackgroundPainter(null);
    }

    private static class MainPanel extends WGridPanel {
        public MainPanel() {
            add(new WText(new TranslatableText("gui.tedium.tabMain")), 0, 0, GRID_WIDTH, 1);

            WButton button = new WButton(new TranslatableText("gui.tedium.button"));
            add(button, 0, 1, 4, 1);
        }
    }

    private static class LocationPanel extends WGridPanel {
        public LocationPanel() {
            add(new WText(new TranslatableText("gui.tedium.tabLocations")), 0, 0, GRID_WIDTH, 1);
        }
    }

    private static class ScriptPanel extends WPlainPanel {
        private final List<ToggleButton> scriptButtons = new ArrayList<>();
        private ScriptFile selectedScript = null;

        public ScriptPanel() {
            add(new WText(new TranslatableText("gui.tedium.tabScripts")), 0, 0, WIDTH, GRID);

            WButton runButton = new TooltipButton()
                    .setTooltip(new TranslatableText("gui.tedium.buttonRun"))
                    .setIcon(new TextureIcon(new Identifier("tedium:textures/gui/play.png")))
                    .setEnabled(false)
                    .setOnClick(() -> {
                Screen screen = MinecraftClient.getInstance().currentScreen;
                if (screen != null) screen.keyPressed(GLFW.GLFW_KEY_ESCAPE, -1, -1);
            });
            add(runButton, 0, GRID, 20, 20);

            WButton folderButton = new TooltipButton()
                    .setTooltip(new TranslatableText("gui.tedium.buttonOpenFolder"))
                    .setIcon(new TextureIcon(new Identifier(Constants.MOD_ID, "textures/gui/folder.png")))
                    .setOnClick(() -> Util.openFile(ModData.getGlobalDir()));
            add(folderButton, 0, HEIGHT, 20, 20);

            List<ScriptFile> listData = Arrays.stream(ModData.getGlobalFiles()).map(ScriptFile::new).collect(Collectors.toList());
            scriptButtons.clear();
            WListPanel<ScriptFile, ToggleButton> scriptList = new WListPanel<>(listData, ToggleButton::new, (file, item) -> {
                item.setLabel(new LiteralText(file.name));
                item.setIcon(new ItemIcon(file.icon));
                item.setOnClick(() -> {
                    runButton.setEnabled(true);
                    selectedScript = file;
                    scriptButtons.forEach(button -> button.setToggled(false));
                    item.setToggled(true);
                });
                scriptButtons.add(item);
            });
            scriptList.setBackgroundPainter(BackgroundPainter.SLOT);
            add(scriptList, (int) (1.5 * GRID), GRID + 1, (GRID_WIDTH - 1) * GRID, HEIGHT);
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
}
