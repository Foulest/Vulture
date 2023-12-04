package net.foulest.vulture.util;

import io.github.retrooper.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.util.data.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MathUtil {

    private static final float[] SIN_TABLE = new float[65536];
    private static final float[] SIN_TABLE_FAST = new float[4096];
    private static final int[] multiplyDeBruijnBitPosition;
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

    public static Vector getDirection(Player player) {
        return new Vector(-Math.sin(player.getEyeLocation().getYaw() * Math.PI / 180.0) * 1.0 * 0.5, 0.0,
                Math.cos(player.getEyeLocation().getYaw() * Math.PI / 180.0) * 1.0 * 0.5);
    }

    public static int getPingInTicks(long ping) {
        return (int) Math.floor(ping / 50.0);
    }

    public static double getStandardDeviation(Collection<? extends Number> data) {
        double variance = getVariance(data);
        return Math.sqrt(variance);
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

    public static long gcd(long a, long b) {
        if (b <= 16384) {
            return a;
        }
        return gcd(b, a % b);
    }

    public static Vector3d getPositionEyes(double x, double y, double z, float eyeHeight) {
        return new Vector3d(x, y + eyeHeight, z);
    }

    public static long getGcd(long current, long previous) {
        return previous <= 0b100000000000000 ? current : getGcd(previous, getDelta(current, previous));
    }

    public static Vector3d getVectorForRotation(float pitch, float yaw) {
        float f = cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -cos(-pitch * 0.017453292F);
        float f3 = sin(-pitch * 0.017453292F);
        return new Vector3d(f1 * f2, f3, f * f2);
    }

    private static long getDelta(long alpha, long beta) {
        return alpha % beta;
    }

    public static <T extends Number> T getMode(Collection<T> collect) {
        Map<T, Integer> repeated = new HashMap<>();

        collect.forEach(val -> {
            int number = repeated.getOrDefault(val, 0);
            repeated.put(val, number + 1);
        });

        return repeated.keySet().stream()
                .map(key -> new Pair<>(key, repeated.get(key)))
                .max(Comparator.comparing(Pair::getY, Comparator.naturalOrder()))
                .orElseThrow(NullPointerException::new).getX();
    }

    /**
     * sin looked up in a table
     */
    public static float sin(float value) {
        return fastMath ? SIN_TABLE_FAST[(int) (value * 651.8986F) & 4095]
                : SIN_TABLE[(int) (value * 10430.378F) & 65535];
    }

    /**
     * cos looked up in the sin table with the appropriate offset
     */
    public static float cos(float value) {
        return fastMath ? SIN_TABLE_FAST[(int) ((value + ((float) Math.PI / 2F)) * 651.8986F) & 4095]
                : SIN_TABLE[(int) (value * 10430.378F + 16384.0F) & 65535];
    }

    /**
     * Returns the greatest integer less than or equal to the double argument
     */
    public static int floorDouble(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static float abs(float value) {
        return value >= 0.0F ? value : -value;
    }

    /**
     * Returns the unsigned value of an int.
     */
    public static int absInt(int value) {
        return value >= 0 ? value : -value;
    }

    /**
     * Returns the input value rounded up to the next highest power of two.
     */
    public static int roundUpToPowerOfTwo(int value) {
        int i = value - 1;
        i = i | i >> 1;
        i = i | i >> 2;
        i = i | i >> 4;
        i = i | i >> 8;
        i = i | i >> 16;
        return i + 1;
    }

    /**
     * Is the given value a power of two?  (1, 2, 4, 8, 16, ...)
     */
    private static boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    /**
     * Uses a B(2, 5) De Bruijn sequence and a lookup table to efficiently calculate the log-base-two of the given
     * value.  Optimized for cases where the input value is a power-of-two.  If the input value is not a power-of-two,
     * then subtract 1 from the return value.
     */
    private static int calculateLogBaseTwoDeBruijn(int value) {
        value = isPowerOfTwo(value) ? value : roundUpToPowerOfTwo(value);
        return multiplyDeBruijnBitPosition[(int) (value * 125613361L >> 27) & 31];
    }

    /**
     * Efficiently calculates the floor of the base-2 log of an integer value.  This is effectively the index of the
     * highest bit that is set.  For example, if the number in binary is 0...100101, this will return 5.
     */
    public static int calculateLogBaseTwo(int value) {
        return calculateLogBaseTwoDeBruijn(value) - (isPowerOfTwo(value) ? 0 : 1);
    }

    public static double pingFormula(long ping) {
        return Math.ceil((ping + 5) / 50.0D);
    }

    public static double getDistanceBetweenAngles360(double angle1, double angle2) {
        double distance = Math.abs(angle1 % 360.0 - angle2 % 360.0);
        distance = Math.min(360.0 - distance, distance);
        return Math.abs(distance);
    }

    public static double getDirection(Location from, Location to) {
        if (from == null || to == null) {
            return 0.0D;
        }

        double difX = to.getX() - from.getX();
        double difZ = to.getZ() - from.getZ();
        return (float) ((Math.atan2(difZ, difX) * 180.0D / Math.PI) - 90.0F);
    }

    public static double hypot(double... value) {
        double total = 0;

        for (double val : value) {
            total += (val * val);
        }
        return Math.sqrt(total);
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

        multiplyDeBruijnBitPosition = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13,
                23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
    }
}
