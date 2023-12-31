package net.foulest.vulture.check.type.killaura;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "KillAura (C)", type = CheckType.KILLAURA)
public class KillAuraC extends Check {

    private double buffer;
    private int ticksSinceHit;
    private double lastDeltaXZ;

    public KillAuraC(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);

            if (flying.isMoving()) {
                Location to = new Location(player.getWorld(), flying.getPosition().getX(), flying.getPosition().getY(),
                        flying.getPosition().getZ(), flying.getYaw(), flying.getPitch());
                Location from = playerData.getLastLocation();

                boolean sprinting = playerData.isSprinting();
                boolean insideVehicle = player.isInsideVehicle();
                boolean onSoulSand = playerData.isOnSoulSand();
                boolean underBlock = playerData.isUnderBlock();

                int totalTicks = playerData.getTotalTicks();
                int lastPacketDrop = playerData.getLastPacketDrop();
                int groundTicks = playerData.getGroundTicks();

                long timeSinceLag = playerData.getTimeSince(ActionType.LAG);

                double deltaX = to.getX() - from.getX();
                double deltaZ = to.getZ() - from.getZ();
                double deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                if (playerData.getLastTarget() == null
                        || playerData.getVelocityY() > 0
                        || playerData.getVelocityXZ() > 0) {
                    lastDeltaXZ = deltaXZ;
                    return;
                }

                if (++ticksSinceHit <= 2 && !playerData.getVersion().isNewerThan(ClientVersion.v_1_15_2)) {
                    if (sprinting && !insideVehicle && totalTicks - lastPacketDrop > 10
                            && timeSinceLag > 100 && !onSoulSand) {
                        double acceleration = Math.abs(deltaXZ - lastDeltaXZ);
                        float speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
                        acceleration += groundTicks < 5 ? speedLevel * 0.07 : speedLevel * 0.0573;

                        if (acceleration <= 0.05 && deltaXZ > 0.23 && !underBlock) {
                            if (++buffer > 8) {
                                flag(false, "acceleration=" + acceleration
                                        + " deltaXZ=" + deltaXZ);
                            }
                        } else {
                            buffer = 0;
                        }
                    } else {
                        buffer = 0;
                    }
                }

                lastDeltaXZ = deltaXZ;
            }

        } else if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            WrappedPacketInUseEntity.EntityUseAction action = useEntity.getAction();

            if (action == WrappedPacketInUseEntity.EntityUseAction.ATTACK) {
                ticksSinceHit = 0;
            }
        }
    }
}
