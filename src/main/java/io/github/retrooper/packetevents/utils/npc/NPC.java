package io.github.retrooper.packetevents.utils.npc;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.play.out.entity.WrappedPacketOutEntity;
import io.github.retrooper.packetevents.packetwrappers.play.out.entitydestroy.WrappedPacketOutEntityDestroy;
import io.github.retrooper.packetevents.packetwrappers.play.out.entityheadrotation.WrappedPacketOutEntityHeadRotation;
import io.github.retrooper.packetevents.packetwrappers.play.out.entityteleport.WrappedPacketOutEntityTeleport;
import io.github.retrooper.packetevents.packetwrappers.play.out.namedentityspawn.WrappedPacketOutNamedEntitySpawn;
import io.github.retrooper.packetevents.packetwrappers.play.out.playerinfo.WrappedPacketOutPlayerInfo;
import io.github.retrooper.packetevents.utils.gameprofile.WrappedGameProfile;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Getter
@Setter
public class NPC {

    private final String name;
    private final int entityId;
    private final UUID uuid;
    private final WrappedGameProfile gameProfile;
    private final Map<UUID, Boolean> spawnedForPlayerMap = new ConcurrentHashMap<>();
    private Vector3d position;
    private float yaw;
    private float pitch;
    private boolean onGround;

    public NPC(String name) {
        this.name = name;
        this.entityId = NMSUtils.generateEntityId();
        this.uuid = NMSUtils.generateUUID();
        this.gameProfile = new WrappedGameProfile(uuid, name);
        this.position = new Vector3d(0, 0, 0);
        this.yaw = 0;
        this.pitch = 0;
    }

    public NPC(String name, int entityId, UUID uuid, WrappedGameProfile gameProfile) {
        this.name = name;
        this.entityId = entityId;
        this.uuid = uuid;
        this.gameProfile = gameProfile;
        this.position = new Vector3d(0, 0, 0);
        this.yaw = 0;
        this.pitch = 0;
    }

    public NPC(String name, Vector3d position, float yaw, float pitch) {
        this.name = name;
        this.entityId = NMSUtils.generateEntityId();
        this.uuid = NMSUtils.generateUUID();
        this.gameProfile = new WrappedGameProfile(uuid, name);
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public NPC(String name, @NotNull Location location) {
        this(name, new Vector3d(location.getX(), location.getY(), location.getZ()), location.getYaw(), location.getPitch());
    }

    public void despawn(@NotNull Player player) {
        boolean spawned = spawnedForPlayerMap.getOrDefault(player.getUniqueId(), false);
        spawnedForPlayerMap.remove(player.getUniqueId());

        if (spawned) {
            try {
                CompletableFuture.runAsync(() -> {
                    WrappedPacketOutPlayerInfo playerInfo = new WrappedPacketOutPlayerInfo(WrappedPacketOutPlayerInfo.PlayerInfoAction.REMOVE_PLAYER, new WrappedPacketOutPlayerInfo.PlayerInfo(name, gameProfile, GameMode.SURVIVAL, 0));
                    PacketEvents.get().getPlayerUtils().sendPacket(player, playerInfo);
                    WrappedPacketOutEntityDestroy wrappedPacketOutEntityDestroy = new WrappedPacketOutEntityDestroy(entityId);
                    PacketEvents.get().getPlayerUtils().sendPacket(player, wrappedPacketOutEntityDestroy);
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasSpawned(@NotNull Player player) {
        return spawnedForPlayerMap.getOrDefault(player.getUniqueId(), false);
    }

    public void spawn(Player player) {
        try {
            if (!hasSpawned(player)) {
                CompletableFuture.runAsync(() -> {
                    WrappedPacketOutPlayerInfo playerInfo = new WrappedPacketOutPlayerInfo(WrappedPacketOutPlayerInfo.PlayerInfoAction.ADD_PLAYER, new WrappedPacketOutPlayerInfo.PlayerInfo(name, gameProfile, GameMode.SURVIVAL, 0));
                    PacketEvents.get().getPlayerUtils().sendPacket(player, playerInfo);
                    WrappedPacketOutNamedEntitySpawn wrappedPacketOutNamedEntitySpawn = new WrappedPacketOutNamedEntitySpawn(entityId, uuid, position, yaw, pitch);
                    PacketEvents.get().getPlayerUtils().sendPacket(player, wrappedPacketOutNamedEntitySpawn);
                    spawnedForPlayerMap.put(player.getUniqueId(), true);
                }).get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public void teleport(Player player, Vector3d targetPosition, float yaw, float pitch) {
        this.position = targetPosition;
        this.yaw = yaw;
        this.pitch = pitch;
        if (hasSpawned(player)) {
            PacketEvents.get().getPlayerUtils().sendPacket(player, new WrappedPacketOutEntityTeleport(entityId, position, yaw, pitch, onGround));
        }
    }

    public void move(Player player, @NotNull Vector3d targetPosition) {
        this.position = targetPosition;
        double distX = targetPosition.x - position.x;
        double distY = targetPosition.y - position.y;
        double distZ = targetPosition.z - position.z;
        double dist = distX + distY + distZ;
        SendableWrapper sentPacket;

        if (dist > 8) {
            sentPacket = new WrappedPacketOutEntityTeleport(entityId, position, yaw, pitch, onGround);
        } else {
            sentPacket = new WrappedPacketOutEntity.WrappedPacketOutRelEntityMove(entityId, distX, distY, distZ, onGround);
        }

        if (hasSpawned(player)) {
            PacketEvents.get().getPlayerUtils().sendPacket(player, sentPacket);
        }
    }

    public void moveAndRotate(Player player, @NotNull Vector3d targetPosition, float yaw, float pitch) {
        this.position = targetPosition;
        this.yaw = yaw;
        this.pitch = pitch;
        double distX = targetPosition.x - position.x;
        double distY = targetPosition.y - position.y;
        double distZ = targetPosition.z - position.z;
        double dist = distX + distY + distZ;
        SendableWrapper sentPacket;

        if (dist > 8) {
            sentPacket = new WrappedPacketOutEntityTeleport(entityId, position, yaw, pitch, onGround);
        } else {
            sentPacket = new WrappedPacketOutEntity.WrappedPacketOutRelEntityMoveLook(entityId, distX, distY, distZ, yaw, pitch, onGround);
        }

        if (hasSpawned(player)) {
            PacketEvents.get().getPlayerUtils().sendPacket(player, sentPacket);
        }
    }

    public void rotate(Player player, float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        WrappedPacketOutEntity.WrappedPacketOutEntityLook lookPacket = new WrappedPacketOutEntity.WrappedPacketOutEntityLook(entityId, (byte) (yaw * 256 / 360), (byte) (pitch * 256 / 360), onGround);
        WrappedPacketOutEntityHeadRotation headRotationPacket = new WrappedPacketOutEntityHeadRotation(entityId, (byte) (yaw * 256 / 360));

        if (hasSpawned(player)) {
            PacketEvents.get().getPlayerUtils().sendPacket(player, lookPacket);
            PacketEvents.get().getPlayerUtils().sendPacket(player, headRotationPacket);
        }
    }

    public void teleport(@NotNull List<Player> players, Vector3d targetPosition, float yaw, float pitch) {
        this.position = targetPosition;
        this.yaw = yaw;
        this.pitch = pitch;

        for (Player player : players) {
            if (hasSpawned(player)) {
                PacketEvents.get().getPlayerUtils().sendPacket(player, new WrappedPacketOutEntityTeleport(entityId, position, yaw, pitch, onGround));
            }
        }
    }

    public void move(List<Player> players, @NotNull Vector3d targetPosition) {
        double distX = targetPosition.x - position.x;
        double distY = targetPosition.y - position.y;
        double distZ = targetPosition.z - position.z;
        double dist = distX + distY + distZ;
        this.position = targetPosition;
        SendableWrapper sentPacket;

        if (dist > 8) {
            sentPacket = new WrappedPacketOutEntityTeleport(entityId, position, yaw, pitch, onGround);
        } else {
            sentPacket = new WrappedPacketOutEntity.WrappedPacketOutRelEntityMove(entityId, distX, distY, distZ, onGround);
        }

        for (Player player : players) {
            if (hasSpawned(player)) {
                PacketEvents.get().getPlayerUtils().sendPacket(player, sentPacket);
            }
        }
    }

    public void moveAndRotate(List<Player> players, @NotNull Vector3d targetPosition, float yaw, float pitch) {
        double distX = targetPosition.x - position.x;
        double distY = targetPosition.y - position.y;
        double distZ = targetPosition.z - position.z;
        double dist = distX + distY + distZ;
        this.position = targetPosition;
        this.yaw = yaw;
        this.pitch = pitch;
        SendableWrapper sentPacket;

        if (dist > 8) {
            sentPacket = new WrappedPacketOutEntityTeleport(entityId, position, yaw, pitch, onGround);
        } else {
            sentPacket = new WrappedPacketOutEntity.WrappedPacketOutRelEntityMoveLook(entityId, distX, distY, distZ, yaw, pitch, onGround);
        }

        for (Player player : players) {
            if (hasSpawned(player)) {
                PacketEvents.get().getPlayerUtils().sendPacket(player, sentPacket);
            }
        }
    }

    public void rotate(@NotNull List<Player> players, float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        WrappedPacketOutEntity.WrappedPacketOutEntityLook lookPacket = new WrappedPacketOutEntity.WrappedPacketOutEntityLook(entityId, (byte) (yaw * 256 / 360), (byte) (pitch * 256 / 360), onGround);
        WrappedPacketOutEntityHeadRotation headRotationPacket = new WrappedPacketOutEntityHeadRotation(entityId, (byte) (yaw * 256 / 360));

        for (Player player : players) {
            if (hasSpawned(player)) {
                PacketEvents.get().getPlayerUtils().sendPacket(player, lookPacket);
                PacketEvents.get().getPlayerUtils().sendPacket(player, headRotationPacket);
            }
        }
    }
}
