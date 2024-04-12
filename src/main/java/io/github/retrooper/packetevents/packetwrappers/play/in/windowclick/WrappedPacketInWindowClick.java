package io.github.retrooper.packetevents.packetwrappers.play.in.windowclick;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.bukkit.inventory.ItemStack;

public class WrappedPacketInWindowClick extends WrappedPacket {

    private static boolean legacy;
    private static boolean v_1_17;
    private static Class<? extends Enum<?>> invClickTypeClass;

    public WrappedPacketInWindowClick(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        legacy = version.isOlderThanOrEquals(ServerVersion.v_1_8_8);
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);
        invClickTypeClass = NMSUtils.getNMSEnumClassWithoutException("InventoryClickType");

        if (invClickTypeClass == null) {
            invClickTypeClass = NMSUtils.getNMEnumClassWithoutException("world.inventory.InventoryClickType");
        }
    }

    // Unique ID for the inventory, 0 for player's inventory
    public int getWindowId() {
        return readInt(v_1_17 ? 1 : 0);
    }

    public void setWindowId(int windowID) {
        writeInt(v_1_17 ? 1 : 0, windowID);
    }

    // ID of clicked slot
    public int getWindowSlot() {
        return readInt(v_1_17 ? 3 : 1);
    }

    public void setWindowSlot(int slot) {
        writeInt(v_1_17 ? 3 : 1, slot);
    }

    // Left or right click
    public int getWindowButton() {
        return readInt(v_1_17 ? 4 : 2);
    }

    public void setWindowButton(int button) {
        writeInt(v_1_17 ? 4 : 2, button);
    }

    // Used to sync together client and server
    public int getActionNumber() {
        if (v_1_17) {
            return readInt(2);
        }
        return readShort(0);
    }

    public void setActionNumber(int actionNumber) {
        if (v_1_17) {
            writeInt(2, actionNumber);
        } else {
            writeShort(0, (short) actionNumber);
        }
    }

    // Type of click - shift clicking, hotbar, drag, pickup...
    public int getMode() {
        if (legacy) {
            return readInt(3);
        } else {
            Enum<?> enumConst = readEnumConstant(0, invClickTypeClass);
            return enumConst.ordinal();
        }
    }

    public void setMode(int mode) {
        if (legacy) {
            writeInt(3, mode);
        } else {
            Enum<?> enumConst = EnumUtil.valueByIndex(invClickTypeClass, mode);
            writeEnumConstant(0, enumConst);
        }
    }

    /**
     * Get the clicked item.
     *
     * @return Get Clicked ItemStack
     */
    public ItemStack getClickedItemStack() {
        return readItemStack(0);
    }

    public void setClickedItemStack(ItemStack stack) {
        writeItemStack(0, stack);
    }
}
