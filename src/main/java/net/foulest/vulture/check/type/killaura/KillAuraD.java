package net.foulest.vulture.check.type.killaura;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.Material;

@CheckInfo(name = "KillAura (D)", type = CheckType.KILLAURA)
public class KillAuraD extends Check {

    private boolean sentDig;
    private int buffer;

    public KillAuraD(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            WrappedPacketInUseEntity.EntityUseAction action = useEntity.getAction();

            if (action == WrappedPacketInUseEntity.EntityUseAction.ATTACK) {
                if (sentDig) {
                    Material heldItemType = player.getItemInHand().getType();

                    if (heldItemType != Material.BOW && heldItemType != Material.FISHING_ROD) {
                        if (++buffer > 6) {
                            flag();
                        }
                    } else {
                        buffer = Math.max(buffer - 1, 0);
                    }
                } else {
                    buffer = Math.max(buffer - 1, 0);
                }
            }

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            sentDig = false;

        } else if (packetId == PacketType.Play.Client.BLOCK_DIG) {
            sentDig = true;
        }
    }
}
