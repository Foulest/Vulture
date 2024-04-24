package io.github.retrooper.packetevents.packetwrappers.play.out.windowitems;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class WrappedPacketOutWindowItems extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int windowID;
    private int stateID;
    private List<ItemStack> slotData;
    private ItemStack heldItem;

    public WrappedPacketOutWindowItems(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.WINDOW_ITEMS.getConstructor();
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

    public int getStateId() {
        if (packet != null) {
            return readInt(1);
        } else {
            return stateID;
        }
    }

    public void setStateId(int stateID) {
        if (packet != null) {
            writeInt(1, stateID);
        } else {
            this.stateID = stateID;
        }
    }

    public List<ItemStack> getSlots() {
        if (packet != null) {
            List<ItemStack> slots = new ArrayList<>();
            Object[] nmsItemStacks = (Object[]) readAnyObject(1);

            for (Object nmsItemStack : nmsItemStacks) {
                slots.add(NMSUtils.toBukkitItemStack(nmsItemStack));
            }
            return slots;
        } else {
            return slotData;
        }
    }

    public void setSlots(List<ItemStack> slots) {
        if (packet != null) {
            Object[] nmsItemStacks = new Object[slots.size()];

            for (int i = 0; i < slots.size(); i++) {
                nmsItemStacks[i] = NMSUtils.toNMSItemStack(slots.get(i));
            }

            writeAnyObject(1, nmsItemStacks);
        } else {
            slotData = slots;
        }
    }

    public Optional<ItemStack> getHeldItem() {
        return Optional.empty();
    }

    public void setHeldItem(ItemStack heldItem) {
        if (packet != null) {
            writeObject(0, NMSUtils.toNMSItemStack(heldItem));
        } else {
            this.heldItem = heldItem;
        }
    }

    private Object getNMSItemHeld() {
        if (packet != null) {
            return readObject(0, NMSUtils.nmsItemStackClass);
        } else {
            return NMSUtils.toNMSItemStack(heldItem);
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object packetInstance = packetConstructor.newInstance();
        WrappedPacketOutWindowItems wrappedPacketOutWindowItems = new WrappedPacketOutWindowItems(new NMSPacket(packetInstance));
        wrappedPacketOutWindowItems.setWindowId(getWindowId());
        wrappedPacketOutWindowItems.setSlots(getSlots());
        return packetInstance;
    }
}
