package net.foulest.vulture.check.type.inventory;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.clientcommand.WrappedPacketInClientCommand;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@SuppressWarnings("deprecation")
@CheckInfo(name = "Inventory (G)", type = CheckType.INVENTORY)
public class InventoryG extends Check {

    private boolean wasOpen;
    private boolean open;

    public InventoryG(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        // Checks the player for exemptions.
        if (playerData.getVersion().isNewerThan(ClientVersion.v_1_8)) {
            return;
        }

        if (packetId == PacketType.Play.Client.CLOSE_WINDOW) {
            if (wasOpen) {
                flag(false, "CLOSE_WINDOW");
            }

        } else if (packetId == PacketType.Play.Client.CLIENT_COMMAND) {
            WrappedPacketInClientCommand clientCommand = new WrappedPacketInClientCommand(nmsPacket);
            WrappedPacketInClientCommand.ClientCommand command = clientCommand.getClientCommand();

            if (command == WrappedPacketInClientCommand.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                open = true;
            }

        } else if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            wasOpen = open;

            if (open) {
                flag(false, "WINDOW_CLICK");
                open = false;
            }

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            open = false;
            wasOpen = false;
        }
    }
}
