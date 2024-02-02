package net.foulest.vulture.check.type.groundspoof;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "GroundSpoof (B)", type = CheckType.GROUNDSPOOF,
        description = "Detects clients sending invalid on-ground Flying packets.")
public class GroundSpoofB extends Check {

    private static final double ON_GROUND_VELOCITY = -0.0784000015258789;

    public GroundSpoofB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            double velocity = player.getVelocity().getY();

            // Checks the player for exemptions.
            if (playerData.getTimeSince(ActionType.RESPAWN) < 1000L
                    || playerData.getTimeSince(ActionType.TELEPORT) < 1000L
                    || playerData.getTimeSince(ActionType.LOGIN) < 1000L
                    || playerData.isNearClimbable()
                    || playerData.isAgainstBlock()
                    || playerData.isNearSlimeBlock()
                    || playerData.isNearGround()) {
                return;
            }

            // Checks for invalid on-ground Flying packets.
            if (!flying.isMoving() && !flying.isRotating() && flying.isOnGround()
                    && velocity != ON_GROUND_VELOCITY) {
                flag(true, "velocity=" + velocity
                        + " airTicks=" + playerData.getAirTicks()
                        + " airTicksStrict=" + playerData.getAirTicksStrict()
                );
            }
        }
    }
}
