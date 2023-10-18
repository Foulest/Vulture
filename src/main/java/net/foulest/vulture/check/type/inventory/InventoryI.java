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
import org.bukkit.inventory.ItemStack;

@CheckInfo(name = "Inventory (I)", type = CheckType.INVENTORY)
public class InventoryI extends Check {

    private int stage;
    private int lastSlot;

    public InventoryI(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (playerData.getVersion().isNewerThanOrEquals(ClientVersion.v_1_9)) {
            return;
        }

        if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            WrappedPacketInWindowClick windowClick = new WrappedPacketInWindowClick(nmsPacket);
            int windowSlot = windowClick.getWindowSlot();
            int windowButton = windowClick.getWindowButton();
            int windowMode = windowClick.getMode();
            ItemStack clickedItem = windowClick.getClickedItemStack();

            if (windowMode == 1 && windowButton == 0) {
                if (stage == 0 && clickedItem == null) {
                    stage = 1;
                } else {
                    if (stage == 1 && clickedItem == null && lastSlot == windowSlot) {
                        flag();
                    }

                    stage = 0;
                }

            } else {
                stage = 0;
            }

            lastSlot = windowSlot;

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            stage = 0;
        }
    }
}
