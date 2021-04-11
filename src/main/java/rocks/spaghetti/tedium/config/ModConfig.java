package rocks.spaghetti.tedium.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.util.ActionResult;
import rocks.spaghetti.tedium.util.Constants;

import java.util.function.Consumer;

@Config(name = Constants.MOD_ID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Excluded
    private static ConfigHolder<ModConfig> configHolder = null;
    @ConfigEntry.Gui.Excluded
    private static Consumer<ModConfig> loadCallback = null;
    @ConfigEntry.Gui.Excluded
    private static Consumer<ModConfig> saveCallback = null;


    private boolean enableFullbright = false;


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
