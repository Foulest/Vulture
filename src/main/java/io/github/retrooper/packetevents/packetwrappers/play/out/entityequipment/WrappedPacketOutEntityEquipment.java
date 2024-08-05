package io.github.retrooper.packetevents.packetwrappers.play.out.entityequipment;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.pair.Pair;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@ToString
public class WrappedPacketOutEntityEquipment extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private List<Pair<EquipmentSlot, ItemStack>> equipment;
    private EquipmentSlot legacySlot;
    private ItemStack legacyItemStack;

    private WrappedPacketOutEntityEquipment(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutEntityEquipment(int entityID, EquipmentSlot slot, ItemStack itemStack) {
        setEntityId(entityID);
        Pair<EquipmentSlot, ItemStack> pair = new Pair<>(slot, itemStack);
        equipment = new ArrayList<>();
        equipment.add(pair);
        legacySlot = slot;
        legacyItemStack = itemStack;
    }

    public WrappedPacketOutEntityEquipment(Entity entity, EquipmentSlot slot, ItemStack itemStack) {
        setEntity(entity);
        Pair<EquipmentSlot, ItemStack> pair = new Pair<>(slot, itemStack);
        equipment = new ArrayList<>();
        equipment.add(pair);
        legacySlot = slot;
        legacyItemStack = itemStack;
    }

    public WrappedPacketOutEntityEquipment(int entityID, @NotNull List<Pair<EquipmentSlot, ItemStack>> equipment) {
        setEntityId(entityID);
        this.equipment = equipment;
        legacySlot = equipment.get(0).getFirst();
        legacyItemStack = equipment.get(0).getSecond();
    }

    public WrappedPacketOutEntityEquipment(Entity entity, @NotNull List<Pair<EquipmentSlot, ItemStack>> equipment) {
        setEntity(entity);
        this.equipment = equipment;
        legacySlot = equipment.get(0).getFirst();
        legacyItemStack = equipment.get(0).getSecond();
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.ENTITY_EQUIPMENT.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    // LEGACY

    private EquipmentSlot getSingleSlot() {
        if (nmsPacket != null) {
            byte id = (byte) readInt(1);
            return EquipmentSlot.getById(id);
        } else {
            return legacySlot;
        }
    }

    private void setSingleSlot(EquipmentSlot slot) {
        if (nmsPacket != null) {
            writeInt(1, slot.getId());
        } else {
            legacySlot = slot;
        }
    }

    private ItemStack getSingleItemStack() {
        if (nmsPacket != null) {
            return readItemStack();
        } else {
            return legacyItemStack;
        }
    }

    private void setSingleItemStack(ItemStack itemStack) {
        if (nmsPacket != null) {
            writeItemStack(itemStack);
        } else {
            legacyItemStack = itemStack;
        }
    }

    private List<Pair<EquipmentSlot, ItemStack>> getEquipment() {
        if (nmsPacket != null) {
            List<Pair<EquipmentSlot, ItemStack>> pair = new ArrayList<>(1);
            pair.add(new Pair<>(getSingleSlot(), getSingleItemStack()));
            return pair;
        } else {
            return equipment;
        }
    }

    private void setEquipment(@NotNull List<Pair<EquipmentSlot, ItemStack>> equipment) {
        if (equipment.size() > 1) {
            throw new UnsupportedOperationException("The equipment pair list size cannot be greater than one on"
                    + " server versions older than 1.16!");
        }

        if (nmsPacket != null) {
            EquipmentSlot equipmentSlot = equipment.get(0).getFirst();
            ItemStack itemStack = equipment.get(0).getSecond();
            setSingleSlot(equipmentSlot);
            setSingleItemStack(itemStack);
        } else {
            this.equipment = equipment;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object packetInstance = packetConstructor.newInstance();
        WrappedPacketOutEntityEquipment wrappedPacketOutEntityEquipment = new WrappedPacketOutEntityEquipment(new NMSPacket(packetInstance));
        wrappedPacketOutEntityEquipment.setEntityId(getEntityId());
        wrappedPacketOutEntityEquipment.setEquipment(getEquipment());
        return packetInstance;
    }

    @Getter
    @ToString
    public enum EquipmentSlot {
        MAINHAND,
        BOOTS,
        LEGGINGS,
        CHESTPLATE,
        HELMET;

        public byte id;

        @Contract(pure = true)
        static @Nullable EquipmentSlot getById(byte id) {
            for (EquipmentSlot slot : values()) {
                if (slot.id == id) {
                    return slot;
                }
            }
            return null;
        }
    }
}
