package net.foulest.vulture.check.type.inventory;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.processor.type.PacketProcessor;

@CheckInfo(name = "Inventory (L)", type = CheckType.INVENTORY,
        description = "Detects sending packets with an open inventory.")
public class InventoryL extends Check {

    private boolean inventoryOpen;

    public InventoryL(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        String packetName = PacketProcessor.getPacketFromId(packetId).getSimpleName();

        // This is a very bare-bones way of checking if an inventory is open.
        // It's not perfect, but it's good enough for clients above 1.8 who don't
        // send the OPEN_INVENTORY_ACHIEVEMENT packet.
        if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            inventoryOpen = true;

        } else if (packetId == PacketType.Play.Client.CLOSE_WINDOW) {
            inventoryOpen = false;

        } else if (inventoryOpen) {
            switch (packetId) {
                case PacketType.Play.Client.FLYING:
                case PacketType.Play.Client.LOOK:
                case PacketType.Play.Client.POSITION:
                case PacketType.Play.Client.POSITION_LOOK:
                case PacketType.Play.Client.ENTITY_ACTION:
                case PacketType.Play.Client.KEEP_ALIVE:
                case PacketType.Play.Client.TRANSACTION:
                case PacketType.Play.Client.STEER_VEHICLE:
                    break;

                default:
                    flag(false, "packet=" + packetName);
                    break;
            }
        }
    }
}
