package net.foulest.vulture.util.data;

import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@SuppressWarnings("unused")
public class MovingObjectPosition {

    private BlockPos blockPos;
    public final MovingObjectType typeOfHit;
    public EnumFacing sideHit;
    public final Vector3d hitVec;
    public Entity entityHit;

    public MovingObjectPosition(Vector3d vector3d, EnumFacing facing,
                                BlockPos blockPos) {
        this(MovingObjectType.BLOCK, vector3d, facing, blockPos);
    }

    public MovingObjectPosition(Vector3d vector3d, EnumFacing facing) {
        this(MovingObjectType.BLOCK, vector3d, facing, BlockPos.ORIGIN);
    }

    @Contract(pure = true)
    public MovingObjectPosition(MovingObjectType typeOfHit, @NotNull Vector3d hitVec,
                                EnumFacing sideHit, BlockPos blockPos) {
        this.typeOfHit = typeOfHit;
        this.blockPos = blockPos;
        this.sideHit = sideHit;
        this.hitVec = new Vector3d(hitVec.x, hitVec.y, hitVec.z);
    }

    public MovingObjectPosition(Entity entityHit, Vector3d hitVec) {
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
