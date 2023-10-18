package net.foulest.vulture.util.block;

import io.github.retrooper.packetevents.utils.vector.Vector3d;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.util.EnumFacing;
import net.foulest.vulture.util.MathUtil;

@Getter
@Setter
public class BlockPos extends Vector3i {

    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
    private static final int NUM_X_BITS = 1 + MathUtil.calculateLogBaseTwo(MathUtil.roundUpToPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final int Y_SHIFT = NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

    public BlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    public BlockPos(double x, double y, double z) {
        super((int) x, (int) y, (int) z);
    }

    public BlockPos(@NonNull Vector3d source) {
        this(source.x, source.y, source.z);
    }

    public BlockPos(@NonNull Vector3i source) {
        this(source.getX(), source.getY(), source.getZ());
    }

    /**
     * Add the given coordinates to the coordinates of this BlockPos
     */
    public BlockPos add(double x, double y, double z) {
        return x == 0.0D && y == 0.0D && z == 0.0D ? this : new BlockPos(getX() + x, getY() + y, getZ() + z);
    }

    /**
     * Add the given coordinates to the coordinates of this BlockPos
     */
    public BlockPos add(int x, int y, int z) {
        return x == 0 && y == 0 && z == 0 ? this : new BlockPos(getX() + x, getY() + y, getZ() + z);
    }

    /**
     * Add the given Vector to this BlockPos
     */
    public BlockPos add(@NonNull Vector3i vec) {
        return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this
                : new BlockPos(getX() + vec.getX(), getY() + vec.getY(), getZ() + vec.getZ());
    }

    /**
     * Subtract the given Vector from this BlockPos
     */
    public BlockPos subtract(@NonNull Vector3i vec) {
        return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this
                : new BlockPos(getX() - vec.getX(), getY() - vec.getY(), getZ() - vec.getZ());
    }

    /**
     * Offset this BlockPos 1 block up
     */
    public BlockPos up() {
        return up(1);
    }

    /**
     * Offset this BlockPos n blocks up
     */
    public BlockPos up(int n) {
        return offset(EnumFacing.UP, n);
    }

    /**
     * Offset this BlockPos 1 block down
     */
    public BlockPos down() {
        return down(1);
    }

    /**
     * Offset this BlockPos n blocks down
     */
    public BlockPos down(int n) {
        return offset(EnumFacing.DOWN, n);
    }

    /**
     * Offset this BlockPos 1 block in northern direction
     */
    public BlockPos north() {
        return north(1);
    }

    /**
     * Offset this BlockPos n blocks in northern direction
     */
    public BlockPos north(int n) {
        return offset(EnumFacing.NORTH, n);
    }

    /**
     * Offset this BlockPos 1 block in southern direction
     */
    public BlockPos south() {
        return south(1);
    }

    /**
     * Offset this BlockPos n blocks in southern direction
     */
    public BlockPos south(int n) {
        return offset(EnumFacing.SOUTH, n);
    }

    /**
     * Offset this BlockPos 1 block in western direction
     */
    public BlockPos west() {
        return west(1);
    }

    /**
     * Offset this BlockPos n blocks in western direction
     */
    public BlockPos west(int n) {
        return offset(EnumFacing.WEST, n);
    }

    /**
     * Offset this BlockPos 1 block in eastern direction
     */
    public BlockPos east() {
        return east(1);
    }

    /**
     * Offset this BlockPos n blocks in eastern direction
     */
    public BlockPos east(int n) {
        return offset(EnumFacing.EAST, n);
    }

    /**
     * Offset this BlockPos 1 block in the given direction
     */
    public BlockPos offset(@NonNull EnumFacing facing) {
        return offset(facing, 1);
    }

    /**
     * Offsets this BlockPos n blocks in the given direction
     */
    public BlockPos offset(@NonNull EnumFacing facing, int n) {
        return n == 0 ? this : new BlockPos(getX() + facing.getFrontOffsetX() * n,
                getY() + facing.getFrontOffsetY() * n,
                getZ() + facing.getFrontOffsetZ() * n);
    }

    /**
     * Calculate the cross product of this and the given Vector
     */
    public BlockPos crossProduct(@NonNull Vector3i vec) {
        return new BlockPos(getY() * vec.getZ() - getZ() * vec.getY(),
                getZ() * vec.getX() - getX() * vec.getZ(),
                getX() * vec.getY() - getY() * vec.getX());
    }

    /**
     * Serialize this BlockPos into a long value
     */
    public long toLong() {
        return (getX() & X_MASK) << X_SHIFT | (getY() & Y_MASK) << Y_SHIFT | (getZ() & Z_MASK);
    }

    /**
     * Create a BlockPos from a serialized long value (created by toLong)
     */
    public static BlockPos fromLong(long serialized) {
        int i = (int) (serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
        int j = (int) (serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
        int k = (int) (serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
        return new BlockPos(i, j, k);
    }

    @Getter
    public static final class MutableBlockPos extends BlockPos {

        private final int x;
        private final int y;
        private final int z;

        public MutableBlockPos(int x, int y, int z) {
            super(0, 0, 0);
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
