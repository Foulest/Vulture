package io.github.retrooper.packetevents.packetwrappers.play.out.setcooldown;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Getter
public class WrappedPacketOutSetCooldown extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private Object nmsItem;
    private int cooldownTicks;

    public WrappedPacketOutSetCooldown(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutSetCooldown(Object nmsItem, int cooldownTicks) {
        this.nmsItem = nmsItem;
        this.cooldownTicks = cooldownTicks;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.SET_COOLDOWN.getConstructor(NMSUtils.nmsItemClass, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isSupported() {
        return version.isNewerThan(ServerVersion.v_1_8_8);
    }

    public ItemStack getItemStack() {
        try {
            Object nmsItem = readObject(0, NMSUtils.nmsItemClass);
            Object nmsItemStack = NMSUtils.itemStackConstructor.newInstance(nmsItem);
            return NMSUtils.toBukkitItemStack(nmsItemStack);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return new ItemStack(Material.AIR);
    }

    public void setNMSItemStack(Object type) {
        if (packet != null) {
            write(NMSUtils.nmsItemClass, 0, type);
        } else {
            this.nmsItem = type;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(nmsItem, getCooldownTicks());
    }

    public int getCooldownTicks() {
        if (packet != null) {
            return readInt(0);
        } else {
            return cooldownTicks;
        }
    }

    public void setCooldownTicks(int cooldownTicks) {
        if (packet != null) {
            writeInt(0, cooldownTicks);
        } else {
            this.cooldownTicks = cooldownTicks;
        }
    }
}
