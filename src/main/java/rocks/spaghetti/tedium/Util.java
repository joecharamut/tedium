package rocks.spaghetti.tedium;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {
    private Util() { throw new IllegalStateException("Utility class"); }

    public static Vec3d applyOffsetWithFacing(Direction facing, Vec3d pos, Vec3d offset) {
        Vec3d outputPos = pos.add(0, offset.getY(), 0);

        Direction.Axis axis = facing.getAxis();
        Direction.AxisDirection axisDirection = facing.getDirection();

        if (axis == Direction.Axis.X) {
            if (axisDirection == Direction.AxisDirection.POSITIVE) {
                outputPos = outputPos.add(offset.getX(), 0, offset.getZ());
            } else {
                outputPos = outputPos.add(-offset.getX(), 0, -offset.getZ());
            }
        } else if (axis == Direction.Axis.Z) {
            if (axisDirection == Direction.AxisDirection.POSITIVE) {
                outputPos = outputPos.add(offset.getZ(), 0, offset.getX());
            } else {
                outputPos = outputPos.add(-offset.getZ(), 0, -offset.getX());
            }
        }

        return outputPos;
    }

    public static BlockPos applyOffsetWithFacing(Direction facing, BlockPos pos, Vec3i offset) {
        return new BlockPos(
                applyOffsetWithFacing(
                        facing,
                        new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                        new Vec3d(offset.getX(), offset.getY(), offset.getZ())
                )
        );
    }

    public static String getResourceAsString(String resource) {
        InputStream is = Util.class.getClassLoader().getResourceAsStream(resource);
        if (is == null) return "";

        StringBuilder sb = new StringBuilder();
        int i;
        try {
            while ((i = is.read()) != -1) {
                sb.append((char) i);
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
}
