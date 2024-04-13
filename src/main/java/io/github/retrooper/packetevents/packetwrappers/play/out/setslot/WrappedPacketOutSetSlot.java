package io.github.retrooper.packetevents.packetwrappers.play.out.setslot;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;

@AllArgsConstructor
public class WrappedPacketOutSetSlot extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int windowID;
    private int slot;
    private ItemStack itemStack;

    public WrappedPacketOutSetSlot(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.SET_SLOT.getConstructor(int.class, int.class, NMSUtils.nmsItemStackClass);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public int getWindowId() {
        if (packet != null) {
            return readInt(0);
        } else {
            return windowID;
        }
    }

    public void setWindowId(int windowID) {
        if (packet != null) {
            writeInt(0, windowID);
        } else {
            this.windowID = windowID;
        }
    }

    public int getSlot() {
        if (packet != null) {
            return readInt(1);
        } else {
            return slot;
        }
    }

    public void setSlot(int slot) {
        if (packet != null) {
            writeInt(1, slot);
        } else {
            this.slot = slot;
        }
    }

    public ItemStack getItemStack() {
        if (packet != null) {
            return readItemStack(0);
        } else {
            return itemStack;
        }
    }

    public void setItemStack(ItemStack itemStack) {
        if (packet != null) {
            writeItemStack(0, itemStack);
        } else {
            this.itemStack = itemStack;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(getWindowId(), getSlot(), NMSUtils.toNMSItemStack(getItemStack()));
    }
}
