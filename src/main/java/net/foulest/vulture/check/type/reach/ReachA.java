package net.foulest.vulture.check.type.reach;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MessageUtil;

@CheckInfo(name = "Reach (A)", type = CheckType.REACH,
        description = "Detects players with invalid reach.")
public class ReachA extends Check {

    public static double maxDistance;
    public static boolean cancelHits;

    public ReachA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            int entityId = useEntity.getEntity().getEntityId();

            // Get tracked entity and perform reach check
            playerData.getEntityTracker().getEntry(entityId).map(playerData::performReachCheck).ifPresent(result -> {
                // If the range is not present, cancel the event.
                if (!result.isPresent()) {
                    event.setCancelled(cancelHits);
                    MessageUtil.debug("Cancelled hit for " + player.getName() + " (invalid range)");
                    return;
                }

                double range = result.get();

                // Flags the player if the range is greater than the max distance.
                if (range > maxDistance) {
                    event.setCancelled(cancelHits);
                    flag(false, "Range: " + range);
                }
            });
        }
    }
}
