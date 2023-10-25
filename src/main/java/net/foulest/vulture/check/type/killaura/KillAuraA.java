package net.foulest.vulture.check.type.killaura;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "KillAura (A)", type = CheckType.KILLAURA)
public class KillAuraA extends Check {

    public Long lastUseEntity;
    private int buffer;

    public KillAuraA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            if (lastUseEntity != null) {
                boolean droppedPackets = playerData.isDroppedPackets();
                boolean hasFast = playerData.hasFast();

                long timeSinceLag = playerData.getTimeSince(ActionType.LAG);

                double delay = System.currentTimeMillis() - lastUseEntity;

                if (delay < 100 && delay > 40 && !hasFast
                        && !droppedPackets && timeSinceLag > 250) {
                    if (++buffer > 3) {
                        flag(false, "delay=" + delay);
                        lastUseEntity = null;
                    }

                } else {
                    buffer = Math.max(buffer - 1, 0);
                }
            }

        } else if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            WrappedPacketInUseEntity.EntityUseAction action = useEntity.getAction();

            if (action == WrappedPacketInUseEntity.EntityUseAction.ATTACK) {
                if (playerData.getTimeSince(ActionType.FLYING_PACKET) < 10) {
                    lastUseEntity = playerData.getTimestamp(ActionType.FLYING_PACKET);
                } else {
                    buffer = Math.max(buffer - 1, 0);
                }
            }
        }
    }
}
