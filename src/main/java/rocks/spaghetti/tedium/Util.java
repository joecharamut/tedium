package rocks.spaghetti.tedium;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.io.*;

public class Util {
    private Util() { throw new IllegalStateException("Utility class"); }

    public static void openFile(File file) {
        net.minecraft.util.Util.getOperatingSystem().open(file);
    }

    public static String readFileToString(File theFile) {
        try {
            InputStream is = new FileInputStream(theFile);
            return readInputStreamToString(is);
        } catch (FileNotFoundException e) {
            Log.catching(e);
        }

        return "";
    }

    public static void writeStringToFile(File theFile, String string) {
        try {
            if (!theFile.exists()) {
                if (!theFile.createNewFile()) {
                    Log.fatal("Error writing to file `{}`!", theFile.toString());
                    return;
                }
            } else {
                if (!theFile.delete()) {
                    Log.fatal("Error writing to file `{}`!", theFile.toString());
                    return;
                }
            }

            try (FileWriter writer = new FileWriter(theFile)) {
                writer.write(string);
            }
        } catch (IOException e) {
            Log.catching(e);
        }
    }

    public static String getResourceAsString(String resource) {
        InputStream is = Util.class.getClassLoader().getResourceAsStream(resource);
        if (is == null) return "";
        return readInputStreamToString(is);
    }

    public static String readInputStreamToString(InputStream stream) {
        StringBuilder sb = new StringBuilder();
        int i;
        try {
            while ((i = stream.read()) != -1) {
                sb.append((char) i);
            }
        } catch (IOException e) {
            Log.catching(e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                Log.catching(e);
            }
        }

        return sb.toString();
    }

    public static byte[] getResourceAsBytes(String resource) {
        InputStream is = Util.class.getClassLoader().getResourceAsStream(resource);
        if (is == null) return new byte[0];

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int i;
        try {
            while ((i = is.read()) != -1) {
                bytes.write(i);
            }
        } catch (IOException e) {
            Log.catching(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.catching(e);
            }
        }

        return bytes.toByteArray();
    }

    @SuppressWarnings("java:S100")
    public static Vec3d Vec3fToVec3d(Vector3f input) {
        return new Vec3d(input.getX(), input.getY(), input.getZ());
    }

    @SuppressWarnings("java:S100")
    public static Vec3i Vec3dToVec3i(Vec3d input) {
        return new Vec3i(input.x, input.y, input.z);
    }

    @SuppressWarnings("java:S100")
    public static Vector3f Vec3dToVec3f(Vec3d input) {
        return new Vector3f((float) input.x, (float) input.y, (float) input.z);
    }

    @SuppressWarnings("java:S100")
    public static Vec3d Vec3iToVec3d(Vec3i input) {
        return new Vec3d(input.getX(), input.getY(), input.getZ());
    }

    public static double round(double input, int decimals) {
        double multiplier = decimals * 10.0D;
        return Math.round(input * multiplier) / multiplier;
    }
}
