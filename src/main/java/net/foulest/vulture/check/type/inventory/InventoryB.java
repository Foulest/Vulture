package net.foulest.vulture.check.type.inventory;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockplace.WrappedPacketInBlockPlace;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.in.windowclick.WrappedPacketInWindowClick;
import io.github.retrooper.packetevents.utils.player.Direction;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@CheckInfo(name = "Inventory (B)", type = CheckType.INVENTORY,
        description = "Detects this Inventory pattern: ClickWindow, HeldItemSlot, BlockPlace, HeldItemSlot")
public class InventoryB extends Check {

    private long start;
    private int stage;

    public InventoryB(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.WINDOW_CLICK) {
            WrappedPacketInWindowClick windowClick = new WrappedPacketInWindowClick(nmsPacket);
            int windowId = windowClick.getWindowId();

            if (windowId == 0) {
                if (stage == 0) {
                    ++stage;
                    start = System.currentTimeMillis();
                }
            }

        } else if (packetId == PacketType.Play.Client.HELD_ITEM_SLOT) {
            if (stage == 1 || stage == 3) {
                ++stage;
            }

        } else if (packetId == PacketType.Play.Client.BLOCK_PLACE) {
            WrappedPacketInBlockPlace blockPlace = new WrappedPacketInBlockPlace(nmsPacket);

            if (blockPlace.getItemStack().isPresent()) {
                ItemStack itemStack = blockPlace.getItemStack().get();
                Material itemType = itemStack.getType();
                Direction direction = blockPlace.getDirection();

                if ((itemType == Material.MUSHROOM_SOUP
                        || itemType == Material.POTION
                        || itemType == Material.BOWL)
                        && direction == Direction.OTHER) {
                    if (stage == 2) {
                        ++stage;
                    }
                }
            }

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);

            if (!flying.isRotating() && !flying.isMoving()) {
                stage = 0;
            }
        }

        long timeDiff = System.currentTimeMillis() - start;

        if (stage == 4 && timeDiff < 350) {
            stage = 0;
            flag(false, "timeDiff=" + timeDiff);
        }
    }
}
