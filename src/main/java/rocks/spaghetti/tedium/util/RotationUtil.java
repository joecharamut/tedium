package rocks.spaghetti.tedium.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtil {
    private RotationUtil() { throw new IllegalStateException("Utility Class"); }

    public static Rotation rotationFromPosition(Vec3d orig, Vec3d dest, Rotation current) {
        return wrapAnglesToRelative(current, calcRotationFromVec3d(orig, dest));
    }

    private static Rotation calcRotationFromVec3d(Vec3d orig, Vec3d dest) {
        double[] delta = {orig.x - dest.x, orig.y - dest.y, orig.z - dest.z};
        double yaw = MathHelper.atan2(delta[0], -delta[2]);
        double dist = Math.sqrt(delta[0] * delta[0] + delta[2] * delta[2]);
        double pitch = MathHelper.atan2(delta[1], dist);
        return new Rotation((float) Math.toDegrees(yaw), (float) Math.toDegrees(pitch));
    }

    public static Rotation wrapAnglesToRelative(Rotation current, Rotation target) {
        if (current.yawIsReallyClose(target)) {
            return new Rotation(current.yaw, target.pitch);
        }
        return target.subtract(current).normalize().add(current);
    }
}
