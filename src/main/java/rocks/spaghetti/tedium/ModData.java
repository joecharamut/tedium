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

        loadGlobal();
    }

    public static void loadGlobal() {
        globalFile = new File(dataDir, "global.json");
        if (globalFile.exists()) {
            GLOBAL = gson.fromJson(Util.readFileToString(globalFile), GlobalData.class);
        } else {
            GLOBAL = new GlobalData();
        }
    }

    public static void saveGlobal() {
        Util.writeStringToFile(globalFile, gson.toJson(GLOBAL));
    }

    public static void loadWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        String filename = "world-";
        if (client.isInSingleplayer()) {
            filename += client.getServer().getSaveProperties().getLevelName();
        } else {
            filename += "Multiplayer_";
            filename += client.getCurrentServerEntry().address;
        }
        filename += ".json";

        worldFile = new File(dataDir, filename);
        if (worldFile.exists()) {
            WORLD = gson.fromJson(Util.readFileToString(worldFile), WorldData.class);
        } else {
            WORLD = new WorldData();
        }
    }

    public static void saveWorld() {
        Util.writeStringToFile(worldFile, gson.toJson(WORLD));
    }

    private static class GlobalData {

    }

    private static class WorldData {

    }
}
