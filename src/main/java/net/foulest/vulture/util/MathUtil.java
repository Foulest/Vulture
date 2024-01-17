package net.foulest.vulture.util;

import io.github.retrooper.packetevents.utils.vector.Vector3d;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

public class MathUtil {

    private static final float[] SIN_TABLE = new float[65536];
    private static final float[] SIN_TABLE_FAST = new float[4096];
    public static boolean fastMath = false;

    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float distance = Math.abs(angle1 - angle2) % 360.0f;

        if (distance > 180.0f) {
            distance = 360.0f - distance;
        }
        return distance;
    }

    public static Vector getVectorSpeed(Vector3d from, Vector3d to) {
        return new Vector(to.getX() - from.getX(), 0, to.getZ() - from.getZ());
    }

    public static Vector getEyeDirection(Player player) {
        return new Vector(-Math.sin(player.getEyeLocation().getYaw() * Math.PI / 180.0) * 1.0 * 0.5, 0.0,
                Math.cos(player.getEyeLocation().getYaw() * Math.PI / 180.0) * 1.0 * 0.5);
    }

    public static int getPingInTicks(long ping) {
        return (int) Math.floor(ping / 50.0);
    }

    public static double getStandardDeviation(Collection<? extends Number> data) {
        return Math.sqrt(getVariance(data));
    }

    public static double getVariance(Collection<? extends Number> data) {
        int count = 0;
        double sum = 0.0;
        double variance = 0.0;
        double average;

        for (Number number : data) {
            sum += number.doubleValue();
            ++count;
        }

        average = sum / count;

        for (Number number : data) {
            variance += Math.pow(number.doubleValue() - average, 2.0);
        }
        return variance;
    }

    public static Vector3d getPositionEyes(double x, double y, double z, float eyeHeight) {
        return new Vector3d(x, y + eyeHeight, z);
    }

    public static Vector3d getVectorForRotation(float pitch, float yaw) {
        float f = cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -cos(-pitch * 0.017453292F);
        float f3 = sin(-pitch * 0.017453292F);
        return new Vector3d(f1 * f2, f3, f * f2);
    }

    public static float sin(float value) {
        return fastMath ? SIN_TABLE_FAST[(int) (value * 651.8986F) & 4095]
                : SIN_TABLE[(int) (value * 10430.378F) & 65535];
    }

    public static float cos(float value) {
        return fastMath ? SIN_TABLE_FAST[(int) ((value + ((float) Math.PI / 2F)) * 651.8986F) & 4095]
                : SIN_TABLE[(int) (value * 10430.378F + 16384.0F) & 65535];
    }

    public static int floorDouble(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static double pingFormula(long ping) {
        return Math.ceil((ping + 5) / 50.0D);
    }

    public static double getDistanceBetweenAngles360(double angle1, double angle2) {
        double distance = Math.abs(angle1 % 360.0 - angle2 % 360.0);
        distance = Math.min(360.0 - distance, distance);
        return Math.abs(distance);
    }

    public static double getEyeDirection(Location from, Location to) {
        if (from == null || to == null) {
            return 0.0D;
        }

        double difX = to.getX() - from.getX();
        double difZ = to.getZ() - from.getZ();
        return (float) ((Math.atan2(difZ, difX) * 180.0D / Math.PI) - 90.0F);
    }

    public static int getPingToTimer(long ping) {
        return (int) ping / 10000;
    }

    static {
        for (int i = 0; i < 65536; ++i) {
            SIN_TABLE[i] = (float) Math.sin(i * Math.PI * 2.0D / 65536.0D);
        }

        for (int j = 0; j < 4096; ++j) {
            SIN_TABLE_FAST[j] = (float) Math.sin(((j + 0.5F) / 4096.0F * ((float) Math.PI * 2F)));
        }

        for (int l = 0; l < 360; l += 90) {
            SIN_TABLE_FAST[(int) (l * 11.377778F) & 4095] = (float) Math.sin((l * 0.017453292F));
        }
    }
}
