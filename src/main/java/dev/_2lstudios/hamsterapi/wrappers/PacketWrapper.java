package dev._2lstudios.hamsterapi.wrappers;

import lombok.Getter;
import dev._2lstudios.hamsterapi.HamsterAPI;
import dev._2lstudios.hamsterapi.enums.PacketType;
import dev._2lstudios.hamsterapi.utils.Reflection;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PacketWrapper {

    private final Class<?> craftItemStackClass;
    private final Class<?> nmsItemStackClass;
    @Getter
    private final Object packet;
    @Getter
    private final String name;
    @Getter
    private final Map<String, String> strings = new HashMap<>();
    private final Map<String, Double> doubles = new HashMap<>();
    @Getter
    private final Map<String, Float> floats = new HashMap<>();
    @Getter
    private final Map<String, Integer> integers = new HashMap<>();
    @Getter
    private final Map<String, Boolean> booleans = new HashMap<>();
    @Getter
    private final Map<String, ItemStack> items = new HashMap<>();
    @Getter
    private final Map<String, Object> objects = new HashMap<>();

    public PacketWrapper(Object packet) {
        Reflection reflection = HamsterAPI.reflection;

        Class<?> minecraftKeyClass = reflection.getMinecraftKey();
        Class<?> packetClass = packet.getClass();
        Class<?> itemStackClass = reflection.getItemStack();

        this.craftItemStackClass = reflection.getCraftItemStack();
        this.nmsItemStackClass = reflection.getItemStack();
        this.packet = packet;
        this.name = packetClass.getSimpleName();

        for (Field field : packetClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                String fieldName = field.getName();
                Object value = field.get(packet);

                if (value instanceof String) {
                    this.strings.put(fieldName, (String) value);
                } else if (value instanceof Integer) {
                    this.integers.put(fieldName, (Integer) value);
                } else if (value instanceof Float) {
                    this.floats.put(fieldName, (Float) value);
                } else if (value instanceof Double) {
                    this.doubles.put(fieldName, (Double) value);
                } else if (value instanceof Boolean) {
                    this.booleans.put(fieldName, (Boolean) value);
                } else if (minecraftKeyClass != null && minecraftKeyClass.isInstance(value)) {
                    this.strings.put(fieldName, value.toString());
                }

                if (itemStackClass.isInstance(value)) {
                    Method asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
                    ItemStack itemStack = (ItemStack) asBukkitCopy.invoke(null, value);

                    this.items.put(fieldName, itemStack);
                    this.objects.put(fieldName, itemStack);
                } else {
                    this.objects.put(fieldName, value);
                }

                field.setAccessible(false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isPacketType(String packetName) {
        return this.name.equals(packetName);
    }

    public boolean isPacketType(PacketType packetType) {
        return this.name.contains(packetType.toString());
    }

    public PacketType getType() {
        for (PacketType packetType : PacketType.values()) {
            if (packetType.name().equals(this.name)) {
                return packetType;
            }
        }
        return null;
    }

    public void write(String key, Object value) {
        try {
            Field field = this.packet.getClass().getDeclaredField(key);
            field.setAccessible(true);
            field.set(packet, value);
            field.setAccessible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void write(String key, ItemStack itemStack) {
        try {
            Field field = this.packet.getClass().getDeclaredField(key);
            Method asNmsCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object nmsItemStack = asNmsCopy.invoke(null, itemStack);

            field.setAccessible(true);
            field.set(packet, nmsItemStack);
            field.setAccessible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getString(String key) {
        return this.strings.get(key);
    }

    public int getInteger(String key) {
        return this.integers.get(key);
    }

    public boolean getBoolean(String key) {
        return this.booleans.get(key);
    }

    public double getDouble(String key) {
        return this.doubles.get(key);
    }

    public float getFloat(String key) {
        return this.floats.get(key);
    }

    public ItemStack getItem(String key) {
        return this.items.get(key);
    }

    public Map<String, Double> getDouble() {
        return this.doubles;
    }

    public String toString() {
        return this.packet.toString();
    }
}
