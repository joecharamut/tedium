package rocks.spaghetti.tedium;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ModData {
    private ModData() { throw new IllegalStateException("Utility Class"); }

    private static final Gson gson = new Gson();
    private static File dataDir;
    private static File globalDir;
    private static File worldDir;
    private static File worldFile;
    private static WorldData WORLD;

    private static void mkdirIfNotExist(File dir) {
        if (!dir.isDirectory() && !dir.mkdir()) {
            Log.fatal("Error creating directory");
        }
    }

    public static void register() {
        dataDir = new File(MinecraftClient.getInstance().getResourcePackDir().getParentFile(), Constants.MOD_ID);
        mkdirIfNotExist(dataDir);
        Log.info("Data directory: {}", dataDir);

        globalDir = new File(dataDir, "global");
        mkdirIfNotExist(globalDir);

        worldDir = new File(dataDir, "world");
        mkdirIfNotExist(worldDir);
    }

    public static File getGlobalDir() {
        return globalDir;
    }

    public static File[] getGlobalFiles() {
        File[] output = new File[0];

        try (Stream<Path> files = Files.walk(globalDir.toPath(), FileVisitOption.FOLLOW_LINKS)) {
            output = files
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .toArray(File[]::new);
        } catch (IOException e) {
            Log.catching(e);
        }

        return output;
    }

    public static void loadWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        String filename = "undefined";

        if (client.isInSingleplayer() && client.getServer() != null) {
            filename =  "world_" + client.getServer().getSaveProperties().getLevelName();
        } else if (client.getCurrentServerEntry() != null) {
            filename = "multiplayer_" + client.getCurrentServerEntry().address;
        }

        filename += ".json";

        worldFile = new File(worldDir, filename);
        if (worldFile.exists()) {
            WORLD = gson.fromJson(Util.readFileToString(worldFile), WorldData.class);
        } else {
            WORLD = new WorldData();
        }
    }

    public static void saveWorld() {
        Util.writeStringToFile(worldFile, gson.toJson(WORLD));
    }

    private static class WorldData {

    }
}
