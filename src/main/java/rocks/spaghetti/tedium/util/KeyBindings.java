package rocks.spaghetti.tedium.util;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import rocks.spaghetti.tedium.gui.ControlGui;
import rocks.spaghetti.tedium.render.DebugHud;

public class KeyBindings {
    private KeyBindings() { throw new IllegalStateException("Utility Class"); }

    public static final KeyBinding openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tedium.openMenu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            Constants.CATEGORY_KEYS
    ));

    public static final KeyBinding toggleDebugKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tedium.toggleDebug",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            Constants.CATEGORY_KEYS
    ));

    public static final KeyBinding testKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tedium.test",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            Constants.CATEGORY_KEYS
    ));

    public static final KeyBinding[] modKeybindings = {
            openMenuKey, toggleDebugKey, testKey
    };

    public static void tick(MinecraftClient client) {
        while (openMenuKey.wasPressed()) {
            client.setScreen(ControlGui.createScreen());
        }

        while (toggleDebugKey.wasPressed()) {
            DebugHud.toggleEnabled();
        }
    }
}
