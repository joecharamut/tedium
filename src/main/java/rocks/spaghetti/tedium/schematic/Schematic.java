package rocks.spaghetti.tedium.schematic;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import rocks.spaghetti.tedium.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schematic {
    private static final Pattern BLOCKSTATE_REGEX = Pattern.compile("(\\w+:\\w+)(\\[(\\w+=\\w+,?\\s*)+])?");
    private static final Pattern PROPERTIES_REGEX = Pattern.compile("(\\w+)=(\\w+)");

    public final int version;
    public final int dataVersion;
    public final Map<String, NbtElement> metadata;
    public final short width;
    public final short length;
    public final short height;
    public final int[] offset;
    public final int paletteMax;
    public final Map<Integer, BlockState> palette;
    public final Map<Vec3i, BlockState> blockData;
//    public final Map<Vec3i, BlockEntity> blockEntities;
//    public final Map<Vec3i, Entity> entities;

    private Schematic(NbtCompound schematic) throws SchematicException {
        version = schematic.getInt("Version");
        if (version != 2) {
            throw new SchematicException(String.format("Unknown Schematic Version (Expected 2, Got: %s)", version));
        }

        dataVersion = schematic.getInt("DataVersion");
        int minecraftVersion = SharedConstants.getGameVersion().getWorldVersion();

        if (dataVersion < minecraftVersion) {
            Log.warn("dataVersion < minecraftVersion, Stuff might be broken!");
        } else if (dataVersion > minecraftVersion) {
            Log.warn("dataVersion > minecraftVersion, Stuff might be broken!");
        }

        HashMap<String, NbtElement> tempMetadata = new HashMap<>();
        if (schematic.contains("Metadata", NbtType.COMPOUND)) {
            NbtCompound meta = schematic.getCompound("Metadata");

            for (String key : meta.getKeys()) {
                tempMetadata.put(key, meta.get(key));
            }
        }
        metadata = tempMetadata;

        width = schematic.getShort("Width");
        height = schematic.getShort("Height");
        length = schematic.getShort("Length");

        if (schematic.contains("Offset", NbtType.LIST)) {
            offset = schematic.getIntArray("Offset");
        } else {
            offset = new int[]{0, 0, 0};
        }

        paletteMax = schematic.getInt("PaletteMax");

        HashMap<Integer, BlockState> tempPalette = new HashMap<>();
        NbtCompound paletteTag = schematic.getCompound("Palette");
        for (String key : paletteTag.getKeys()) {
            int index = paletteTag.getInt(key);
            Matcher blockMatch = BLOCKSTATE_REGEX.matcher(key);
            if (!blockMatch.matches()) throw new SchematicException(String.format("Invalid BlockState while parsing Palette: %s", key));

            String blockId = blockMatch.group(1);
            Registry.BLOCK
                    .getOrEmpty(new Identifier(blockId))
                    .orElseThrow(() -> new SchematicException(String.format("Invalid Block ID while parsing Palette: %s", blockId)));
            NbtCompound blockTag = new NbtCompound();
            blockTag.putString("Name", blockId);

            String propList = blockMatch.group(2);
            NbtCompound propTag = new NbtCompound();
            if (propList != null) {
                Matcher propMatch = PROPERTIES_REGEX.matcher(propList);
                while (propMatch.find()) {
                    propTag.putString(propMatch.group(1), propMatch.group(2));
                }
            }
            blockTag.put("Properties", propTag);
            tempPalette.put(index, NbtHelper.toBlockState(blockTag));
        }
        palette = ImmutableMap.copyOf(tempPalette);

        byte[] rawBlockData = schematic.getByteArray("BlockData");
        HashMap<Vec3i, BlockState> tempBlockData = new HashMap<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = x + z * width + y * width * length;
                    int block = rawBlockData[index];
                    tempBlockData.put(new Vec3i(x, y, z), palette.get(block));
                }
            }
        }
        blockData = ImmutableMap.copyOf(tempBlockData);

    }

    public static Schematic load(File schematicFile) throws SchematicException {
        if (!schematicFile.getName().endsWith(".schem") || !schematicFile.isFile() || !schematicFile.canRead()) {
            throw new SchematicException("Invalid Schematic File");
        }

        NbtCompound schematic;
        try {
            schematic = NbtIo.readCompressed(schematicFile);
        } catch (IOException e) {
            Log.catching(e);
            throw new SchematicException("Error loading schematic");
        }

        return new Schematic(schematic);
    }
}
