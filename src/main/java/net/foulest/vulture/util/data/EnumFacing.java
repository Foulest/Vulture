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
package net.foulest.vulture.util.data;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.*;
import net.foulest.vulture.util.MathUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

@Getter
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@SuppressWarnings("unused")
public enum EnumFacing implements IStringSerializable {
    DOWN("DOWN", 0, 1, -1, "down", AxisDirection.NEGATIVE, Axis.Y, new Vector3i(0, -1, 0)),
    UP("UP", 1, 0, -1, "up", AxisDirection.POSITIVE, Axis.Y, new Vector3i(0, 1, 0)),
    NORTH("NORTH", 2, 3, 2, "north", AxisDirection.NEGATIVE, Axis.Z, new Vector3i(0, 0, -1)),
    SOUTH("SOUTH", 3, 2, 0, "south", AxisDirection.POSITIVE, Axis.Z, new Vector3i(0, 0, 1)),
    WEST("WEST", 4, 5, 1, "west", AxisDirection.NEGATIVE, Axis.X, new Vector3i(-1, 0, 0)),
    EAST("EAST", 5, 4, 3, "east", AxisDirection.POSITIVE, Axis.X, new Vector3i(1, 0, 0));

    private final String directionName;

    /**
     * Ordering index for D-U-N-S-W-E
     */
    private final int index;

    /**
     * Index of the opposite Facing in the VALUES array
     */
    private final int opposite;

    /**
     * Ordering index for the HORIZONTALS field (S-W-N-E)
     */
    private final int horizontalIndex;

    @ToString.Include
    private final String name;
    private final AxisDirection axisDirection;
    private final Axis axis;

    /**
     * Normalized Vector that points in the direction of this Facing
     */
    private final Vector3i directionVec;

    /**
     * All facings in D-U-N-S-W-E order
     */
    private static final EnumFacing[] FACINGS = new EnumFacing[6];

    /**
     * All Facings with horizontal axis in order S-W-N-E
     */
    private static final EnumFacing[] HORIZONTALS = new EnumFacing[4];

    private static final Map<Object, Object> NAME_LOOKUP = Maps.newHashMap();

    /**
     * Get the opposite Facing (e.g. DOWN => UP)
     *
     * @return The opposite Facing
     */
    public EnumFacing getOpposite() {
        return FACINGS[opposite];
    }

    /**
     * Rotate this Facing around the given axis clockwise.
     * <p>
     * If this facing cannot be rotated around the given axis, returns this facing without rotating.
     *
     * @param axis The axis to rotate around
     * @return The rotated Facing
     */
    public EnumFacing rotateAround(@NotNull Axis axis) {
        switch (EnumFacingUtils.AXIS_ORDINAL_MAP[axis.ordinal()]) {
            case 1:
                if (this != WEST && this != EAST) {
                    return rotateX();
                }
                return this;

            case 2:
                if (this != UP && this != DOWN) {
                    return rotateY();
                }
                return this;

            case 3:
                if (this != NORTH && this != SOUTH) {
                    return rotateZ();
                }
                return this;

            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    /**
     * Rotate this Facing around the X axis (NORTH => DOWN => SOUTH => UP => NORTH)
     *
     * @return The rotated Facing
     */
    private EnumFacing rotateX() {
        switch (EnumFacingUtils.FACING_ORDINAL_MAP[ordinal()]) {
            case 1:
                return DOWN;
            case 3:
                return UP;
            case 5:
                return NORTH;
            case 6:
                return SOUTH;
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
        }
    }

    /**
     * Rotate this Facing around the Y axis clockwise (NORTH => EAST => SOUTH => WEST => NORTH)
     *
     * @return The rotated Facing
     */
    private EnumFacing rotateY() {
        switch (EnumFacingUtils.FACING_ORDINAL_MAP[ordinal()]) {
            case 1:
                return EAST;
            case 2:
                return SOUTH;
            case 3:
                return WEST;
            case 4:
                return NORTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

    /**
     * Rotate this Facing around the Z axis (EAST => DOWN => WEST => UP => EAST)
     *
     * @return The rotated Facing
     */
    private EnumFacing rotateZ() {
        switch (EnumFacingUtils.FACING_ORDINAL_MAP[ordinal()]) {
            case 2:
                return DOWN;
            case 4:
                return UP;
            case 5:
                return EAST;
            case 6:
                return WEST;
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
        }
    }

    /**
     * Rotate this Facing around the Y axis counter-clockwise (NORTH => WEST => SOUTH => EAST => NORTH)
     *
     * @return The rotated Facing
     */
    public EnumFacing rotateYCCW() {
        switch (EnumFacingUtils.FACING_ORDINAL_MAP[ordinal()]) {
            case 1:
                return WEST;
            case 2:
                return NORTH;
            case 3:
                return EAST;
            case 4:
                return SOUTH;
            default:
                throw new IllegalStateException("Unable to get CCW facing of " + this);
        }
    }

    /**
     * Returns an offset that addresses the block in front of this facing.
     */
    public int getFrontOffsetX() {
        return axis == Axis.X ? axisDirection.getOffset() : 0;
    }

    public int getFrontOffsetY() {
        return axis == Axis.Y ? axisDirection.getOffset() : 0;
    }

    /**
     * Returns an offset that addresses the block in front of this facing.
     */
    public int getFrontOffsetZ() {
        return axis == Axis.Z ? axisDirection.getOffset() : 0;
    }

    /**
     * Get the facing specified by the given name
     */
    public static EnumFacing byName(@NotNull String name) {
        return (EnumFacing) NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Get a Facing by its index (0-5). The order is D-U-N-S-W-E. Named getFront for legacy reasons.
     */
    public static EnumFacing getFront(int index) {
        return FACINGS[Math.abs(index % FACINGS.length)];
    }

    /**
     * Get a Facing by its horizontal index (0-3). The order is S-W-N-E.
     */
    private static EnumFacing getHorizontal(int index) {
        return HORIZONTALS[Math.abs(index % HORIZONTALS.length)];
    }

    /**
     * Get the Facing corresponding to the given angle (0-360). An angle of 0 is SOUTH, an angle of 90 would be WEST.
     */
    public static EnumFacing fromAngle(double angle) {
        return getHorizontal(MathUtil.floorDouble(angle / 90.0D + 0.5D) & 3);
    }

    /**
     * Choose a random Facing using the given Random
     */
    public static EnumFacing random(@NotNull Random random) {
        return values()[random.nextInt(values().length)];
    }

    public static EnumFacing getFacingFromVector(float f1, float f2, float f3) {
        EnumFacing north = NORTH;
        float minValue = Float.MIN_VALUE;

        for (EnumFacing facing : values()) {
            float f4 = f1 * facing.directionVec.getX()
                    + f2 * facing.directionVec.getY()
                    + f3 * facing.directionVec.getZ();

            if (f4 > minValue) {
                minValue = f4;
                north = facing;
            }
        }
        return north;
    }

    public static @NotNull EnumFacing getFacingByDirectionAndAxis(AxisDirection direction, Axis axis) {
        for (EnumFacing facing : values()) {
            if (facing.axisDirection == direction && facing.axis == axis) {
                return facing;
            }
        }
        throw new IllegalArgumentException("No such direction: " + direction + " " + axis);
    }

    static {
        for (EnumFacing facing : values()) {
            FACINGS[facing.index] = facing;

            if (facing.axis.isHorizontal()) {
                HORIZONTALS[facing.horizontalIndex] = facing;
            }

            NAME_LOOKUP.put(facing.name.toLowerCase(Locale.ROOT), facing);
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class EnumFacingUtils {

        static final int[] AXIS_ORDINAL_MAP;
        static final int[] FACING_ORDINAL_MAP;
        static final int[] PLANE_ORDINAL_MAP = new int[Plane.values().length];

        static {
            try {
                PLANE_ORDINAL_MAP[Plane.HORIZONTAL.ordinal()] = 1;
                PLANE_ORDINAL_MAP[Plane.VERTICAL.ordinal()] = 2;
            } catch (NoSuchFieldError ex) {
                ex.printStackTrace();
            }

            FACING_ORDINAL_MAP = new int[values().length];

            try {
                FACING_ORDINAL_MAP[NORTH.ordinal()] = 1;
                FACING_ORDINAL_MAP[EAST.ordinal()] = 2;
                FACING_ORDINAL_MAP[SOUTH.ordinal()] = 3;
                FACING_ORDINAL_MAP[WEST.ordinal()] = 4;
                FACING_ORDINAL_MAP[UP.ordinal()] = 5;
                FACING_ORDINAL_MAP[DOWN.ordinal()] = 6;
            } catch (NoSuchFieldError ex) {
                ex.printStackTrace();
            }

            AXIS_ORDINAL_MAP = new int[Axis.values().length];

            try {
                AXIS_ORDINAL_MAP[Axis.X.ordinal()] = 1;
                AXIS_ORDINAL_MAP[Axis.Y.ordinal()] = 2;
                AXIS_ORDINAL_MAP[Axis.Z.ordinal()] = 3;
            } catch (NoSuchFieldError ex) {
                ex.printStackTrace();
            }
        }
    }

    @Getter
    @AllArgsConstructor
    @ToString(onlyExplicitlyIncluded = true)
    public enum Axis implements Predicate<Object>, IStringSerializable {
        X("X", 0, "x", Plane.HORIZONTAL),
        Y("Y", 1, "y", Plane.VERTICAL),
        Z("Z", 2, "z", Plane.HORIZONTAL);

        private final String axisName;
        private final int index;
        @ToString.Include
        private final String name;
        private final Plane plane;

        private static final Map<String, Axis> NAME_LOOKUP = Maps.newHashMap();

        public static Axis byName(@NotNull String name) {
            return NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
        }

        public boolean isVertical() {
            return plane == Plane.VERTICAL;
        }

        boolean isHorizontal() {
            return plane == Plane.HORIZONTAL;
        }

        @Contract(pure = true)
        boolean apply(@NotNull EnumFacing facing) {
            return facing.getAxis() == this;
        }

        public boolean apply(Object object) {
            return apply((EnumFacing) object);
        }

        static {
            for (Axis axis1 : values()) {
                NAME_LOOKUP.put(axis1.name.toLowerCase(Locale.ROOT), axis1);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    @ToString(onlyExplicitlyIncluded = true)
    public enum AxisDirection {
        POSITIVE("POSITIVE", 0, 1, "Towards positive"),
        NEGATIVE("NEGATIVE", 1, -1, "Towards negative");

        private final String name;
        private final int index;
        private final int offset;
        @ToString.Include
        private final String description;
    }

    @Getter
    @ToString
    @AllArgsConstructor
    public enum Plane implements Predicate<Object>, Iterable<EnumFacing> {
        HORIZONTAL("HORIZONTAL", 0),
        VERTICAL("VERTICAL", 1);

        private final String name;
        private final int index;

        @Contract(" -> new")
        EnumFacing @NotNull [] facings() {
            switch (EnumFacingUtils.PLANE_ORDINAL_MAP[ordinal()]) {
                case 1:
                    return new EnumFacing[]{NORTH, EAST, SOUTH, WEST};
                case 2:
                    return new EnumFacing[]{UP, DOWN};
                default:
                    throw new IllegalStateException("Unable to get axis planes for " + this);
            }
        }

        public EnumFacing random(@NotNull Random random) {
            EnumFacing[] facings = facings();
            return facings[random.nextInt(facings.length)];
        }

        boolean apply(@NotNull EnumFacing facing) {
            return facing.getAxis().getPlane() == this;
        }

        @SuppressWarnings("NullableProblems")
        public Iterator<EnumFacing> iterator() {
            return Iterators.forArray(facings());
        }

        public boolean apply(Object obj) {
            return apply((EnumFacing) obj);
        }
    }
}
