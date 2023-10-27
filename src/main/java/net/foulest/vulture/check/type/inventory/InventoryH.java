package net.foulest.vulture.check.type.inventory;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.windowclick.WrappedPacketInWindowClick;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "Inventory (H)", type = CheckType.INVENTORY)
public class InventoryH extends Check {

    private boolean pressed;
    private int lastSlot;
    private int lastButton;

    public InventoryH(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        // Checks the player for exemptions.
        if (!playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8)) {
            return;
        }

        if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            WrappedPacketInWindowClick windowClick = new WrappedPacketInWindowClick(nmsPacket);
            int windowSlot = windowClick.getWindowSlot();
            int windowButton = windowClick.getWindowButton();
            int windowId = windowClick.getWindowId();
            int windowMode = windowClick.getMode();

            if (windowId != 0 && windowMode == 2) {
                if (pressed && lastSlot != windowSlot && lastButton == windowButton) {
                    flag(false);
                }

                pressed = true;
            }

            lastSlot = windowSlot;
            lastButton = windowButton;

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            pressed = false;
        }
    }
}
