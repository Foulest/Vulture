package net.foulest.vulture.check.type.badpackets;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.processor.type.PacketProcessor;

@CheckInfo(name = "BadPackets (A)", type = CheckType.BADPACKETS,
        description = "Detects sending Post packets.")
public class BadPacketsA extends Check {

    private long lastFlying;

    public BadPacketsA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            lastFlying = System.currentTimeMillis();

        } else if (packetId != PacketType.Play.Client.KEEP_ALIVE
                && packetId != PacketType.Play.Client.TRANSACTION
                && packetId != PacketType.Play.Client.ARM_ANIMATION
                && packetId != PacketType.Play.Client.STEER_VEHICLE
                && packetId != PacketType.Play.Client.SETTINGS
                && packetId != PacketType.Play.Client.ENTITY_ACTION
                && packetId != PacketType.Play.Client.BLOCK_DIG
                && packetId != PacketType.Play.Client.BLOCK_PLACE) {
            long timeSinceLag = playerData.getTimeSince(ActionType.LAG);

            int lastServerPositionTick = playerData.getLastServerPositionTick();
            int totalTicks = playerData.getTotalTicks();
            int lastPacketDrop = playerData.getLastPacketDrop();

            double flyingDiff = (System.currentTimeMillis() - lastFlying);

            if (flyingDiff < 9.0 && timeSinceLag > 200L
                    && lastServerPositionTick > 20
                    && totalTicks - lastPacketDrop > 20) {
                flag("Post Packet"
                        + " packet=" + PacketProcessor.getPacketFromId(packetId).getSimpleName()
                        + " flyingDiff=" + flyingDiff);
            }
        }
    }
}
