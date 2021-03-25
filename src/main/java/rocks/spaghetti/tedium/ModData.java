package rocks.spaghetti.tedium;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;

import java.io.File;

public class ModData {
    private ModData() { throw new IllegalStateException("Utility Class"); }

    private static final Gson gson = new Gson();
    private static File dataDir;
    private static File globalFile;
    private static File worldFile;

    public static GlobalData GLOBAL = null;
    public static WorldData WORLD = null;

    public static void register() {
        dataDir = new File(MinecraftClient.getInstance().getResourcePackDir().getParentFile(), Constants.MOD_ID);
        if (!dataDir.isDirectory() && !dataDir.mkdir()) {
            Log.fatal("Error creating data directory!");
        }
        Log.info("Data directory: {}", dataDir);
        globalFile = new File(dataDir, "global.json");

        loadGlobal();
    }

    public static void loadGlobal() {
        if (globalFile.exists()) {
            GLOBAL = gson.fromJson(Util.readFileToString(globalFile), GlobalData.class);
        } else {
            GLOBAL = new GlobalData();
        }
    }

    public static void saveGlobal() {
        Util.writeStringToFile(globalFile, gson.toJson(GLOBAL));
    }

    private static class GlobalData {

    }

    private static class WorldData {

    }
}
