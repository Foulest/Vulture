package net.foulest.vulture.util.data;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.util.MathUtil;
import net.foulest.vulture.util.MessageUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

@Getter
@AllArgsConstructor
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
    public static final EnumFacing[] VALUES = new EnumFacing[6];

    /**
     * All Facings with horizontal axis in order S-W-N-E
     */
    private static final EnumFacing[] HORIZONTALS = new EnumFacing[4];

    private static final Map<Object, Object> NAME_LOOKUP = Maps.newHashMap();

    /**
     * Get the opposite Facing (e.g. DOWN => UP)
     */
    public EnumFacing getOpposite() {
        return VALUES[opposite];
    }

    /**
     * Rotate this Facing around the given axis clockwise. If this facing cannot be rotated around the given axis,
     * returns this facing without rotating.
     */
    public EnumFacing rotateAround(@NotNull Axis axis) {
        switch (EnumFacing$1.AXIS_ORDINAL_MAP[axis.ordinal()]) {
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
     */
    private EnumFacing rotateX() {
        switch (EnumFacing$1.FACING_ORDINAL_MAP[ordinal()]) {
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
     */
    public EnumFacing rotateY() {
        switch (EnumFacing$1.FACING_ORDINAL_MAP[ordinal()]) {
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
     */
    private EnumFacing rotateZ() {
        switch (EnumFacing$1.FACING_ORDINAL_MAP[ordinal()]) {
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
     */
    public EnumFacing rotateYCCW() {
        switch (EnumFacing$1.FACING_ORDINAL_MAP[ordinal()]) {
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
        return (EnumFacing) NAME_LOOKUP.get(name.toLowerCase());
    }

    /**
     * Get a Facing by its index (0-5). The order is D-U-N-S-W-E. Named getFront for legacy reasons.
     */
    public static EnumFacing getFront(int index) {
        return VALUES[Math.abs(index % VALUES.length)];
    }

    /**
     * Get a Facing by its horizontal index (0-3). The order is S-W-N-E.
     */
    public static EnumFacing getHorizontal(int index) {
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

    public String toString() {
        return name;
    }

    public static @NotNull EnumFacing getFacingByDirectionAndAxis(AxisDirection direction, Axis axis) {
        for (EnumFacing facing : values()) {
            if (facing.getAxisDirection() == direction && facing.getAxis() == axis) {
                return facing;
            }
        }
        throw new IllegalArgumentException("No such direction: " + direction + " " + axis);
    }

    static {
        for (EnumFacing facing : values()) {
            VALUES[facing.index] = facing;

            if (facing.getAxis().isHorizontal()) {
                HORIZONTALS[facing.horizontalIndex] = facing;
            }

            NAME_LOOKUP.put(facing.getName().toLowerCase(), facing);
        }
    }

    @Getter
    @Setter
    static final class EnumFacing$1 {

        static final int[] AXIS_ORDINAL_MAP;
        static final int[] FACING_ORDINAL_MAP;
        static final int[] PLANE_ORDINAL_MAP = new int[Plane.values().length];

        static {
            try {
                PLANE_ORDINAL_MAP[Plane.HORIZONTAL.ordinal()] = 1;
                PLANE_ORDINAL_MAP[Plane.VERTICAL.ordinal()] = 2;
            } catch (NoSuchFieldError ex) {
                MessageUtil.printException(ex);
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
                MessageUtil.printException(ex);
            }

            AXIS_ORDINAL_MAP = new int[Axis.values().length];

            try {
                AXIS_ORDINAL_MAP[Axis.X.ordinal()] = 1;
                AXIS_ORDINAL_MAP[Axis.Y.ordinal()] = 2;
                AXIS_ORDINAL_MAP[Axis.Z.ordinal()] = 3;
            } catch (NoSuchFieldError ex) {
                MessageUtil.printException(ex);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Axis implements Predicate<Object>, IStringSerializable {
        X("X", 0, "x", Plane.HORIZONTAL),
        Y("Y", 1, "y", Plane.VERTICAL),
        Z("Z", 2, "z", Plane.HORIZONTAL);

        private final String axisName;
        private final int index;
        private final String name;
        private final Plane plane;

        private static final Map<String, Axis> NAME_LOOKUP = Maps.newHashMap();

        public static Axis byName(@NotNull String name) {
            return NAME_LOOKUP.get(name.toLowerCase());
        }

        public boolean isVertical() {
            return plane == Plane.VERTICAL;
        }

        public boolean isHorizontal() {
            return plane == Plane.HORIZONTAL;
        }

        public String toString() {
            return name;
        }

        @Contract(pure = true)
        public boolean apply(@NotNull EnumFacing facing) {
            return facing.getAxis() == this;
        }

        public boolean apply(Object object) {
            return apply((EnumFacing) object);
        }

        static {
            for (Axis axis1 : values()) {
                NAME_LOOKUP.put(axis1.getName().toLowerCase(), axis1);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public enum AxisDirection {
        POSITIVE("POSITIVE", 0, 1, "Towards positive"),
        NEGATIVE("NEGATIVE", 1, -1, "Towards negative");

        private final String name;
        private final int index;
        private final int offset;
        private final String description;

        public String toString() {
            return description;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Plane implements Predicate<Object>, Iterable<EnumFacing> {
        HORIZONTAL("HORIZONTAL", 0),
        VERTICAL("VERTICAL", 1);

        private final String name;
        private final int index;

        @Contract(" -> new")
        public EnumFacing @NotNull [] facings() {
            switch (EnumFacing$1.PLANE_ORDINAL_MAP[ordinal()]) {
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

        public boolean apply(@NotNull EnumFacing facing) {
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

