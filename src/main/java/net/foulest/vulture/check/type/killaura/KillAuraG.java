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

@CheckInfo(name = "KillAura (G)", type = CheckType.KILLAURA, acceptsServerPackets = true)
public class KillAuraG extends Check {

    private long lastFlyingTime = -1;
    private double buffer;

    public KillAuraG(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            lastFlyingTime = System.currentTimeMillis();

        } else if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            WrappedPacketInUseEntity.EntityUseAction action = useEntity.getAction();

            long timeSinceLag = playerData.getTimeSince(ActionType.LAG);
            long timeSinceAttacking = playerData.getTimeSince(ActionType.ATTACKING);

            int lastServerPositionTick = playerData.getLastServerPositionTick();
            int totalTicks = playerData.getTotalTicks();
            int lastPacketDrop = playerData.getLastPacketDrop();

            if (action == WrappedPacketInUseEntity.EntityUseAction.ATTACK
                    && timeSinceLag > 200 && timeSinceAttacking < 1000
                    && lastServerPositionTick > 60 && totalTicks - lastPacketDrop > 20) {
                long timeDelta = System.currentTimeMillis() - lastFlyingTime;

                if (timeDelta < 5) {
                    if (++buffer > 10) {
                        flag(false, "timeDelta=" + timeDelta);
                    }
                } else {
                    buffer = 0;
                }
            }

        } else if (packetId == PacketType.Play.Server.POSITION) {
            buffer = 0;
        }
    }
}
