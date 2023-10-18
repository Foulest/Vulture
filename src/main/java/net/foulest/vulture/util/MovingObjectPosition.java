package net.foulest.vulture.util;

import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.util.block.BlockPos;
import org.bukkit.entity.Entity;

@Getter
@Setter
public class MovingObjectPosition {

    private BlockPos blockPos;
    public final MovingObjectType typeOfHit;
    public EnumFacing sideHit;
    public final Vector3d hitVec;
    public Entity entityHit;

    public MovingObjectPosition(@NonNull Vector3d vector3d, @NonNull EnumFacing facing,
                                @NonNull BlockPos blockPos) {
        this(MovingObjectType.BLOCK, vector3d, facing, blockPos);
    }

    public MovingObjectPosition(@NonNull Vector3d vector3d, @NonNull EnumFacing facing) {
        this(MovingObjectType.BLOCK, vector3d, facing, BlockPos.ORIGIN);
    }

    public MovingObjectPosition(@NonNull MovingObjectType typeOfHit, @NonNull Vector3d hitVec,
                                @NonNull EnumFacing sideHit, @NonNull BlockPos blockPos) {
        this.typeOfHit = typeOfHit;
        this.blockPos = blockPos;
        this.sideHit = sideHit;
        this.hitVec = new Vector3d(hitVec.x, hitVec.y, hitVec.z);
    }

    public MovingObjectPosition(@NonNull Entity entityHit, @NonNull Vector3d hitVec) {
        typeOfHit = MovingObjectType.ENTITY;
        this.entityHit = entityHit;
        this.hitVec = hitVec;
    }

    public enum MovingObjectType {
        MISS,
        BLOCK,
        ENTITY
    }
}

