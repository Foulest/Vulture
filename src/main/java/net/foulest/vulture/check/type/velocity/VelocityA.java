package net.foulest.vulture.check.type.velocity;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "Velocity (A)", type = CheckType.VELOCITY,
        description = "Checks for incorrect vertical velocity.")
public class VelocityA extends Check {

    private double lastY;

    public VelocityA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            Vector3d flyingPosition = flying.getPosition();
            double deltaY = (flying.isMoving() ? flyingPosition.getY() - lastY : 0.0);
            double takenVelocity = playerData.getVelocityY();
            double threshold = 0.003017;

            // Checks the player for exemptions.
            if (playerData.isNearLiquid()
                    || playerData.isUnderBlock()
                    || playerData.isInWeb()
                    || playerData.isOnClimbable()
                    || player.isInsideVehicle()
                    || takenVelocity <= 0.0) {
                lastY = (flying.isMoving() ? flyingPosition.getY() : lastY);
                return;
            }

            if (Math.abs(deltaY - takenVelocity) > threshold) {
                flag(true, "deltaY=" + deltaY
                        + " takenVelocity=" + takenVelocity
                );
            } else {
                debug("deltaY=" + deltaY
                        + " takenVelocity=" + takenVelocity
                );
            }

            lastY = (flying.isMoving() ? flyingPosition.getY() : lastY);
        }
    }
}
