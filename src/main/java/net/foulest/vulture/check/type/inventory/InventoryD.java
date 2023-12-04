package net.foulest.vulture.check.type.inventory;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.clientcommand.WrappedPacketInClientCommand;
import io.github.retrooper.packetevents.packetwrappers.play.in.closewindow.WrappedPacketInCloseWindow;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.in.windowclick.WrappedPacketInWindowClick;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "Inventory (D)", type = CheckType.INVENTORY,
        description = "Detects this Inventory pattern: OpenInventory, WindowClick (Quick Move/Throw), CloseWindow")
public class InventoryD extends Check {

    private int stage;

    public InventoryD(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        // Checks the player for exemptions.
        if (!playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8)) {
            return;
        }

        if (packetId == PacketType.Play.Client.CLIENT_COMMAND) {
            WrappedPacketInClientCommand clientCommand = new WrappedPacketInClientCommand(nmsPacket);
            WrappedPacketInClientCommand.ClientCommand command = clientCommand.getClientCommand();

            if (command == WrappedPacketInClientCommand.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT && stage == 0) {
                ++stage;
            }

        } else if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            WrappedPacketInWindowClick windowClick = new WrappedPacketInWindowClick(nmsPacket);
            int windowId = windowClick.getWindowId();
            int windowMode = windowClick.getMode();

            if (windowId == 0 && (windowMode == 1 || windowMode == 4) && stage == 1) {
                ++stage;
            }

        } else if (packetId == PacketType.Play.Client.CLOSE_WINDOW) {
            WrappedPacketInCloseWindow closeWindow = new WrappedPacketInCloseWindow(nmsPacket);
            int windowId = closeWindow.getWindowId();

            if (windowId == 0 && stage == 2) {
                ++stage;
            }

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);

            if (flying.isMoving()) {
                stage = 0;
            }
        }

        if (stage == 3) {
            stage = 0;
            flag(false);
        }
    }
}
