package net.foulest.vulture.check.type.flight;

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

@CheckInfo(name = "Flight (C)", type = CheckType.FLIGHT,
        description = "Checks for ignoring gravity completely.")
public class FlightC extends Check {

    private static final double ON_GROUND_VELOCITY = -0.0784000015258789;
    private double lastY;
    private double lastVelocity;
    private int ticksInAir;

    public FlightC(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            Vector3d flyingPosition = flying.getPosition();
            double deltaY = flyingPosition.getY() - lastY;
            double velocity = player.getVelocity().getY();

            if (playerData.isNearbyBoat(0.6, 0.6, 0.6)
                    || playerData.isNearSlimeBlock()) {
                lastY = flyingPosition.getY();
                lastVelocity = velocity;
                return;
            }

            if (velocity != ON_GROUND_VELOCITY && !playerData.isOnGround()
                    && velocity != lastVelocity && deltaY == 0.0
                    && !playerData.isNearbyBoat(0.6, 0.6, 0.6)) {
                if (++ticksInAir >= 4) {
                    flag(true, "ticks=" + ticksInAir
                            + " velocity=" + velocity
                            + " lastVelocity=" + lastVelocity
                            + " deltaY=" + deltaY
                            + " Y=" + flyingPosition.getY()
                            + " onGround=" + playerData.isOnGround()
                            + " nearGround=" + playerData.isNearGround()
                            + " againstBlock=" + playerData.isAgainstBlock()
                            + " underBlock=" + playerData.isUnderBlock()
                    );
                }
            } else {
                ticksInAir = 0;
            }

            lastY = flyingPosition.getY();
            lastVelocity = velocity;
        }
    }
}
