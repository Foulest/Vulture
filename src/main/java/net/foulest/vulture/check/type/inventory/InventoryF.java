package net.foulest.vulture.check.type.inventory;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "Inventory (F)", type = CheckType.INVENTORY,
        description = "Detects swinging & clicking in your inventory at the same time.")
public class InventoryF extends Check {

    private boolean click;
    private boolean swing;

    public InventoryF(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.ARM_ANIMATION) {
            swing = true;

        } else if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            click = true;

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            if (swing && click) {
                flag(false);
            }

            swing = false;
            click = false;
        }
    }
}
