package dev._2lstudios.hamsterapi.wrappers;

import dev._2lstudios.hamsterapi.HamsterAPI;
import dev._2lstudios.hamsterapi.enums.PacketType;
import dev._2lstudios.hamsterapi.utils.Reflection;
import lombok.Getter;
import net.foulest.vulture.util.MessageUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Getter
public class PacketWrapper {

    private final Object packet;
    private final String name;
    private final Class<?> craftItemStackClass;

    private final Map<String, String> strings = new HashMap<>();
    private final Map<String, Double> doubles = new HashMap<>();
    private final Map<String, Float> floats = new HashMap<>();
    private final Map<String, Integer> integers = new HashMap<>();
    private final Map<String, Boolean> booleans = new HashMap<>();
    private final Map<String, ItemStack> items = new HashMap<>();
    private final Map<String, Object> objects = new HashMap<>();

    public PacketWrapper(@NotNull Object packet) {
        Reflection reflection = HamsterAPI.reflection;

        Class<?> minecraftKeyClass = reflection.getMinecraftKey();
        Class<?> packetClass = packet.getClass();
        Class<?> itemStackClass = reflection.getItemStack();
        Class<?> nmsItemStackClass = reflection.getItemStack();

        this.packet = packet;
        name = packetClass.getSimpleName();
        craftItemStackClass = reflection.getCraftItemStack();

        for (Field field : packetClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                String fieldName = field.getName();
                Object value = field.get(packet);

                if (value instanceof String) {
                    strings.put(fieldName, (String) value);
                } else if (value instanceof Integer) {
                    integers.put(fieldName, (Integer) value);
                } else if (value instanceof Float) {
                    floats.put(fieldName, (Float) value);
                } else if (value instanceof Double) {
                    doubles.put(fieldName, (Double) value);
                } else if (value instanceof Boolean) {
                    booleans.put(fieldName, (Boolean) value);
                } else if (minecraftKeyClass != null && minecraftKeyClass.isInstance(value)) {
                    strings.put(fieldName, value.toString());
                }

                if (itemStackClass.isInstance(value)) {
                    Method asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
                    ItemStack itemStack = (ItemStack) asBukkitCopy.invoke(null, value);

                    items.put(fieldName, itemStack);
                    objects.put(fieldName, itemStack);
                } else {
                    objects.put(fieldName, value);
                }

                field.setAccessible(false);
            } catch (Exception ex) {
                MessageUtil.printException(ex);
            }
        }
    }

    public boolean isPacketType(String packetName) {
        return name.equals(packetName);
    }

    public boolean isPacketType(@NotNull PacketType packetType) {
        return name.contains(packetType.toString());
    }

    public PacketType getType() {
        for (PacketType packetType : PacketType.values()) {
            if (packetType.name().equals(name)) {
                return packetType;
            }
        }
        return null;
    }

    public void write(String key, Object value) {
        try {
            Field field = packet.getClass().getDeclaredField(key);
            field.setAccessible(true);
            field.set(packet, value);
            field.setAccessible(false);
        } catch (Exception ex) {
            MessageUtil.printException(ex);
        }
    }

    public void write(String key, ItemStack itemStack) {
        try {
            Field field = packet.getClass().getDeclaredField(key);
            Method asNmsCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object nmsItemStack = asNmsCopy.invoke(null, itemStack);

            field.setAccessible(true);
            field.set(packet, nmsItemStack);
            field.setAccessible(false);
        } catch (Exception ex) {
            MessageUtil.printException(ex);
        }
    }

    public String getString(String key) {
        return strings.get(key);
    }

    public int getInteger(String key) {
        return integers.get(key);
    }

    public boolean getBoolean(String key) {
        return booleans.get(key);
    }

    public double getDouble(String key) {
        return doubles.get(key);
    }

    public float getFloat(String key) {
        return floats.get(key);
    }

    public ItemStack getItem(String key) {
        return items.get(key);
    }

    public Map<String, Double> getDouble() {
        return doubles;
    }

    public String toString() {
        return packet.toString();
    }
}
