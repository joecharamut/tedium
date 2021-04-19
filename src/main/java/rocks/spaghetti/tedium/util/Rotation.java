package rocks.spaghetti.tedium.util;

import net.minecraft.util.math.MathHelper;

public class Rotation {
    public final float pitch;
    public final float yaw;

    public Rotation(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;

        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            throw new IllegalArgumentException(String.format("Invalid Rotation arguments: %s, %s", yaw, pitch));
        }
    }

    public Rotation add(Rotation other) {
        return new Rotation(this.yaw + other.yaw, this.pitch + other.pitch);
    }

    public Rotation subtract(Rotation other) {
        return new Rotation(this.yaw - other.yaw, this.pitch - other.pitch);
    }

    public Rotation clamp() {
        return new Rotation(this.yaw, clampPitch(this.pitch));
    }

    public Rotation normalize() {
        return new Rotation(normalizeYaw(this.yaw), this.pitch);
    }

    public Rotation normalizeAndClamp() {
        return new Rotation(normalizeYaw(this.yaw), clampPitch(this.pitch));
    }

    public boolean isReallyCloseTo(Rotation other) {
        return yawIsReallyClose(other) && MathHelper.approximatelyEquals(this.pitch, other.pitch);
    }

    public boolean yawIsReallyClose(Rotation other) {
        return MathHelper.approximatelyEquals(normalizeYaw(this.yaw), normalizeYaw(other.yaw));
    }

    public static float clampPitch(float pitch) {
        return MathHelper.clamp(pitch, -90, 90);
    }

    public static float normalizeYaw(float yaw) {
        yaw %= 360;
        if (yaw < -180) yaw += 360;
        if (yaw >  180) yaw -= 360;
        return yaw;
    }

    @Override
    public String toString() {
        return String.format("Pitch: %.2f Yaw: %.2f", pitch, yaw);
    }
}

