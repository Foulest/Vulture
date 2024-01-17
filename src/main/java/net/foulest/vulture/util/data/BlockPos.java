package net.foulest.vulture.util.data;

import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockPos extends Vector3i {

    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);

    public BlockPos(int x, int y, int z) {
        super(x, y, z);
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
