package net.foulest.vulture.check.type.badpackets;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.processor.type.PacketProcessor;

@CheckInfo(name = "BadPackets (B)", type = CheckType.BADPACKETS,
        description = "Detects sending invalid packets while in a bed.")
public class BadPacketsB extends Check {

    private int ticksInBed;

    public BadPacketsB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        ticksInBed = !playerData.isInBed() ? 0 : ticksInBed + 1;

        // Checks the player for exemptions.
        if (ticksInBed < 10) {
            return;
        }

        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            Vector3d flyingPosition = flying.getPosition();

            if (flying.isRotating()) {
                flag(true, "Sent invalid Rotation packet while in bed" + " (ticks=" + ticksInBed + ")");
            }

            if (flying.isMoving() && playerData.isMoving() && !playerData.isTeleporting(flyingPosition)) {
                flag(true, "Sent invalid Position packet while in bed" + " (ticks=" + ticksInBed + ")");
            }

        } else if (packetId != PacketType.Play.Client.CHAT
                && packetId != PacketType.Play.Client.KEEP_ALIVE) {
            flag(false, event, "Sent invalid packet while in bed: " + PacketProcessor.getPacketFromId(packetId).getSimpleName());
        }
    }
}
