package net.foulest.vulture.check.type.groundspoof;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "GroundSpoof (B)", type = CheckType.GROUNDSPOOF,
        description = "Detects clients sending invalid on-ground Flying packets.")
public class GroundSpoofB extends Check {

    public GroundSpoofB(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (playerData.getTimeSince(ActionType.RESPAWN) < 1000L
                || playerData.getTimeSince(ActionType.TELEPORT) < 1000L
                || playerData.getTimeSince(ActionType.LOGIN) < 1000L
                || playerData.isAgainstBlock()
                || playerData.isOnClimbable()) {
            return;
        }

        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            double velocityY = player.getVelocity().getY();
            int airTicksStrict = playerData.getAirTicksStrict();

            if (!flying.isMoving() && !flying.isRotating() && flying.isOnGround() && velocityY < 0
                    && Math.abs(velocityY) > 0.1552320045166016 && airTicksStrict > 6) {
                if (velocityY == -0.5169479491049732 && airTicksStrict == 16) {
                    return;
                }

                flag("Sent empty Flying packet while falling"
                        + " (velocityY=" + velocityY
                        + " airTicksStrict=" + airTicksStrict + ")"
                );
            }
        }
    }
}
