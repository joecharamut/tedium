package rocks.spaghetti.tedium.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.Option;
import net.minecraft.util.ActionResult;
import rocks.spaghetti.tedium.Constants;
import rocks.spaghetti.tedium.Log;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Config(name = Constants.MOD_ID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Excluded
    private static ConfigHolder<ModConfig> configHolder = null;
    @ConfigEntry.Gui.Excluded
    private static Consumer<ModConfig> loadCallback = null;
    @ConfigEntry.Gui.Excluded
    private static Consumer<ModConfig> saveCallback = null;
    @ConfigEntry.Gui.Excluded
    private static final int DEFAULT_WEB_SERVER_PORT = 8383;


    private boolean webServerEnabled = true;
    private int webServerPort = DEFAULT_WEB_SERVER_PORT;
    private boolean enableFullbright = false;


    public static int getWebServerPort() {
        return getConfig().webServerPort;
    }

    public static boolean isWebServerEnabled() {
        return getConfig().webServerEnabled;
    }

    public static boolean isFullbrightEnabled() {
        return getConfig().enableFullbright;
    }

    public static void register(Consumer<ModConfig> loadCallback, Consumer<ModConfig> saveCallback) {
        ModConfig.loadCallback = loadCallback;
        ModConfig.saveCallback = saveCallback;
        configHolder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        configHolder.registerLoadListener(ModConfig::configLoadListener);
        configHolder.registerSaveListener(ModConfig::configSaveListener);
    }

    public static ModConfig getConfig() {
        return configHolder.getConfig();
    }

    @Override
    public void validatePostLoad() throws ValidationException {
        if (webServerPort < 1024 || webServerPort > 48000) {
            Log.warn("Invalid Web Server Port: Value must be between 1024 and 48000, set to default.");
            webServerPort = DEFAULT_WEB_SERVER_PORT;
        }
    }

    private static ActionResult configLoadListener(ConfigHolder<ModConfig> holder, ModConfig config) {
        configHolder = holder;

        if (ModConfig.loadCallback != null) {
            ModConfig.loadCallback.accept(config);
        }

        return ActionResult.PASS;
    }

    private static ActionResult configSaveListener(ConfigHolder<ModConfig> holder, ModConfig config) {
        if (ModConfig.saveCallback != null) {
            ModConfig.saveCallback.accept(config);
        }

        return ActionResult.PASS;
    }
}
