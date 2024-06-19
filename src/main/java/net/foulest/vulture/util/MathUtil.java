/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.vulture.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.foulest.vulture.util.data.Area;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MathUtil {

    private static final float[] SIN_TABLE = new float[65536];

    static {
        for (int i = 0; i < SIN_TABLE.length; ++i) {
            SIN_TABLE[i] = (float) Math.sin(i * Math.PI * 2.0D / 65536.0D);
        }
    }

    @Contract(pure = true)
    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float distance = Math.abs(angle1 - angle2) % 360.0f;

        if (distance > 180.0f) {
            distance = 360.0f - distance;
        }
        return distance;
    }

    public static double getStandardDeviation(Collection<? extends Number> data) {
        return Math.sqrt(getVariance(data));
    }

    public static double getVariance(@NotNull Collection<? extends Number> data) {
        int count = 0;
        double sum = 0.0;
        double variance = 0.0;
        double average;

        for (Number number : data) {
            sum += number.doubleValue();
            ++count;
        }

        if (count == 0) {
            return 0.0;
        }

        average = sum / count;

        for (Number number : data) {
            variance += Math.pow(number.doubleValue() - average, 2.0);
        }
        return variance;
    }

    @Contract(pure = true)
    public static float sin(float value) {
        return SIN_TABLE[(int) (value * 10430.378F) & 65535];
    }

    @Contract(pure = true)
    public static float cos(float value) {
        return SIN_TABLE[(int) (value * 10430.378F + 16384.0F) & 65535];
    }

    @Contract(pure = true)
    public static int floorDouble(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    @Contract(pure = true)
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

    public static org.joml.Vector3d calculateIntercept(@NotNull Area area, org.joml.Vector3d pos, org.joml.Vector3d ray) {
        org.joml.Vector3d vec3 = getIntermediateWithXValue(pos, ray, area.getMin().getX());
        org.joml.Vector3d vec31 = getIntermediateWithXValue(pos, ray, area.getMax().getX());
        org.joml.Vector3d vec32 = getIntermediateWithYValue(pos, ray, area.getMin().getY());
        org.joml.Vector3d vec33 = getIntermediateWithYValue(pos, ray, area.getMax().getY());
        org.joml.Vector3d vec34 = getIntermediateWithZValue(pos, ray, area.getMin().getZ());
        org.joml.Vector3d vec35 = getIntermediateWithZValue(pos, ray, area.getMax().getZ());

        if (!isVecInYZ(area, vec3)) {
            vec3 = null;
        }

        if (!isVecInYZ(area, vec31)) {
            vec31 = null;
        }

        if (!isVecInXZ(area, vec32)) {
            vec32 = null;
        }

        if (!isVecInXZ(area, vec33)) {
            vec33 = null;
        }

        if (!isVecInXY(area, vec34)) {
            vec34 = null;
        }

        if (!isVecInXY(area, vec35)) {
            vec35 = null;
        }

        org.joml.Vector3d vec36 = null;

        if (vec3 != null) {
            vec36 = vec3;
        }

        if (vec31 != null && (vec36 == null || pos.distanceSquared(vec31) < pos.distanceSquared(vec36))) {
            vec36 = vec31;
        }

        if (vec32 != null && (vec36 == null || pos.distanceSquared(vec32) < pos.distanceSquared(vec36))) {
            vec36 = vec32;
        }

        if (vec33 != null && (vec36 == null || pos.distanceSquared(vec33) < pos.distanceSquared(vec36))) {
            vec36 = vec33;
        }

        if (vec34 != null && (vec36 == null || pos.distanceSquared(vec34) < pos.distanceSquared(vec36))) {
            vec36 = vec34;
        }

        if (vec35 != null && (vec36 == null || pos.distanceSquared(vec35) < pos.distanceSquared(vec36))) {
            vec36 = vec35;
        }
        return vec36;
    }

    @Contract(value = "_, null -> false", pure = true)
    private static boolean isVecInYZ(Area area, org.joml.Vector3d vec) {
        return vec != null && vec.y >= area.getMin().getY() && vec.y <= area.getMax().getY()
                && vec.z >= area.getMin().getZ() && vec.z <= area.getMax().getZ();
    }

    @Contract(value = "_, null -> false", pure = true)
    private static boolean isVecInXZ(Area area, org.joml.Vector3d vec) {
        return vec != null && vec.x >= area.getMin().getX() && vec.x <= area.getMax().getX()
                && vec.z >= area.getMin().getZ() && vec.z <= area.getMax().getZ();
    }

    @Contract(value = "_, null -> false", pure = true)
    private static boolean isVecInXY(Area area, org.joml.Vector3d vec) {
        return vec != null && vec.x >= area.getMin().getX() && vec.x <= area.getMax().getX()
                && vec.y >= area.getMin().getY() && vec.y <= area.getMax().getY();
    }

    @Contract(pure = true)
    public static org.joml.@Nullable Vector3d getIntermediateWithXValue(@NotNull org.joml.Vector3d vec1,
                                                                        @NotNull org.joml.Vector3d vec2, double limit) {
        double d0 = vec2.x - vec1.x;
        double d1 = vec2.y - vec1.y;
        double d2 = vec2.z - vec1.z;

        if (d0 * d0 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (limit - vec1.x) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new org.joml.Vector3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3) : null;
        }
    }

    @Contract(pure = true)
    public static org.joml.@Nullable Vector3d getIntermediateWithYValue(@NotNull org.joml.Vector3d vec1,
                                                                        @NotNull org.joml.Vector3d vec2, double limit) {
        double d0 = vec2.x - vec1.x;
        double d1 = vec2.y - vec1.y;
        double d2 = vec2.z - vec1.z;

        if (d1 * d1 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (limit - vec1.y) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new org.joml.Vector3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3) : null;
        }
    }

    @Contract(pure = true)
    public static org.joml.@Nullable Vector3d getIntermediateWithZValue(@NotNull org.joml.Vector3d vec1,
                                                                        @NotNull org.joml.Vector3d vec2, double limit) {
        double d0 = vec2.x - vec1.x;
        double d1 = vec2.y - vec1.y;
        double d2 = vec2.z - vec1.z;

        if (d2 * d2 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (limit - vec1.z) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new org.joml.Vector3d(vec1.x + d0 * d3, vec1.y + d1 * d3, vec1.z + d2 * d3) : null;
        }
    }

    @Contract("_, _ -> new")
    public static org.joml.@NotNull Vector3d getLookVector(float yaw, float pitch) {
        float f = MathUtil.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathUtil.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathUtil.cos(-pitch * 0.017453292F);
        float f3 = MathUtil.sin(-pitch * 0.017453292F);
        return new org.joml.Vector3d(f1 * f2, f3, f * f2);
    }

    public static double getGcd(double a, double b) {
        while (b > 0.001) {
            double temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    public static float getGcd(float a, float b) {
        while (b > 0.001F) {
            float temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    public static <T extends Number> T getMode(@NotNull Collection<T> collect) {
        Map<T, Integer> repeated = collect.stream()
                .collect(Collectors.toMap(Function.identity(), v -> 1, Integer::sum));

        return repeated.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(NullPointerException::new);
    }
}
