package io.github.retrooper.packetevents.packetwrappers.play.out.windowitems;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO: Test wrapper
public class WrappedPacketOutWindowItems extends WrappedPacket implements SendableWrapper {

    private static boolean v_1_17;
    private static boolean v_1_17_1;
    private static Object nonNullListInstance;
    private static Class<?> nonNullListClass;
    private static Constructor<?> packetConstructor;

    private int windowID;
    private int stateID;
    private List<ItemStack> slotData;
    private ItemStack heldItem;

    public WrappedPacketOutWindowItems(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutWindowItems(int windowID, int stateID, List<ItemStack> slots, ItemStack heldItem) {
        this.windowID = windowID;
        this.stateID = stateID;
        this.slotData = slots;
        this.heldItem = heldItem;
    }

    @Override
    protected void load() {
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);
        v_1_17_1 = version.isNewerThanOrEquals(ServerVersion.v_1_17_1);

        try {
            if (v_1_17) {
                nonNullListClass = NMSUtils.getNMClassWithoutException("core.NonNullList");

                if (v_1_17_1) {
                    Constructor<?> nonNullListConstructor = nonNullListClass.getDeclaredConstructors()[0];
                    nonNullListConstructor.setAccessible(true);
                    nonNullListInstance = nonNullListConstructor.newInstance(new ArrayList<>(), null);
                    packetConstructor = PacketTypeClasses.Play.Server.WINDOW_ITEMS.getConstructor(int.class, int.class, nonNullListClass, NMSUtils.nmsItemStackClass);
                } else {
                    Constructor<?> nonNullListConstructor = nonNullListClass.getDeclaredConstructor();
                    nonNullListConstructor.setAccessible(true);
                    nonNullListInstance = nonNullListConstructor.newInstance();
                    packetConstructor = PacketTypeClasses.Play.Server.WINDOW_ITEMS.getConstructor(int.class, nonNullListClass);
                }
            } else {
                packetConstructor = PacketTypeClasses.Play.Server.WINDOW_ITEMS.getConstructor();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 InstantiationException e) {
            e.printStackTrace();
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

            if (version.isNewerThan(ServerVersion.v_1_10_2)) {
                List<Object> nmsItemStacks = readList(0);

                for (Object nmsItemStack : nmsItemStacks) {
                    slots.add(NMSUtils.toBukkitItemStack(nmsItemStack));
                }
            } else {
                Object[] nmsItemStacks = (Object[]) readAnyObject(1);

                for (Object nmsItemStack : nmsItemStacks) {
                    slots.add(NMSUtils.toBukkitItemStack(nmsItemStack));
                }
            }
            return slots;
        } else {
            return slotData;
        }
    }

    public void setSlots(List<ItemStack> slots) {
        if (packet != null) {
            if (version.isNewerThan(ServerVersion.v_1_10_2)) {
                List<Object> nmsItemStacks = new ArrayList<>();

                for (ItemStack itemStack : slots) {
                    nmsItemStacks.add(NMSUtils.toNMSItemStack(itemStack));
                }

                writeList(0, nmsItemStacks);

            } else {
                Object[] nmsItemStacks = new Object[slots.size()];

                for (int i = 0; i < slots.size(); i++) {
                    nmsItemStacks[i] = NMSUtils.toNMSItemStack(slots.get(i));
                }

                writeAnyObject(1, nmsItemStacks);
            }
        } else {
            this.slotData = slots;
        }
    }

    public Optional<ItemStack> getHeldItem() {
        if (!v_1_17_1) return Optional.empty();

        if (packet != null) {
            return Optional.ofNullable(NMSUtils.toBukkitItemStack(readObject(0, NMSUtils.nmsItemStackClass)));
        } else {
            return Optional.of(heldItem);
        }
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
        Object packetInstance;

        if (v_1_17_1) {
            packetInstance = packetConstructor.newInstance(getWindowId(), stateID, nonNullListInstance, getNMSItemHeld());
            WrappedPacketOutWindowItems wrappedPacketOutWindowItems = new WrappedPacketOutWindowItems(new NMSPacket(packetInstance));
            wrappedPacketOutWindowItems.setSlots(getSlots());

        } else if (v_1_17) {
            packetInstance = packetConstructor.newInstance(getWindowId(), nonNullListInstance);
            WrappedPacketOutWindowItems wrappedPacketOutWindowItems = new WrappedPacketOutWindowItems(new NMSPacket(packetInstance));
            wrappedPacketOutWindowItems.setSlots(getSlots());

        } else {
            packetInstance = packetConstructor.newInstance();
            WrappedPacketOutWindowItems wrappedPacketOutWindowItems = new WrappedPacketOutWindowItems(new NMSPacket(packetInstance));
            wrappedPacketOutWindowItems.setWindowId(getWindowId());
            wrappedPacketOutWindowItems.setSlots(getSlots());
        }
        return packetInstance;
    }
}
