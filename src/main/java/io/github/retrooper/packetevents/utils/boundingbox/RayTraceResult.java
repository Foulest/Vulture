package io.github.retrooper.packetevents.utils.boundingbox;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
@ToString
public class RayTraceResult {

    private final Vector hitPosition;
    private final Block hitBlock;
    private final BlockFace hitBlockFace;
    private final Entity hitEntity;

    private RayTraceResult(@NotNull Vector hitPosition, @Nullable Block hitBlock,
                           @Nullable BlockFace hitBlockFace, @Nullable Entity hitEntity) {
        this.hitPosition = hitPosition.clone();
        this.hitBlock = hitBlock;
        this.hitBlockFace = hitBlockFace;
        this.hitEntity = hitEntity;
    }

    public RayTraceResult(@NotNull Vector hitPosition) {
        this(hitPosition, null, null, null);
    }

    public RayTraceResult(@NotNull Vector hitPosition, @Nullable BlockFace hitBlockFace) {
        this(hitPosition, null, hitBlockFace, null);
    }

    public RayTraceResult(@NotNull Vector hitPosition, @Nullable Block hitBlock, @Nullable BlockFace hitBlockFace) {
        this(hitPosition, hitBlock, hitBlockFace, null);
    }

    public RayTraceResult(@NotNull Vector hitPosition, @Nullable Entity hitEntity) {
        this(hitPosition, null, null, hitEntity);
    }

    public RayTraceResult(@NotNull Vector hitPosition, @Nullable Entity hitEntity, @Nullable BlockFace hitBlockFace) {
        this(hitPosition, null, hitBlockFace, hitEntity);
    }

    public Vector getHitPosition() {
        return hitPosition.clone();
    }

    public int hashCode() {
        int result = 31 + hitPosition.hashCode();
        result = 31 * result + (hitBlock == null ? 0 : hitBlock.hashCode());
        result = 31 * result + (hitBlockFace == null ? 0 : hitBlockFace.hashCode());
        result = 31 * result + (hitEntity == null ? 0 : hitEntity.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof RayTraceResult)) {
            return false;
        } else {
            RayTraceResult other = (RayTraceResult) obj;

            if (!hitPosition.equals(other.hitPosition)) {
                return false;
            } else if (!Objects.equals(hitBlock, other.hitBlock)) {
                return false;
            } else if (!Objects.equals(hitBlockFace, other.hitBlockFace)) {
                return false;
            } else {
                return Objects.equals(hitEntity, other.hitEntity);
            }
        }
    }
}
