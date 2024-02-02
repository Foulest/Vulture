package dev._2lstudios.hamsterapi.utils;

import lombok.AllArgsConstructor;
import net.foulest.vulture.util.MessageUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@AllArgsConstructor
public class Reflection {

    private final String version;
    private final Map<String, Class<?>> classes = new HashMap<>();
    private final Map<Class<?>, Map<Class<?>, Map<Integer, Field>>> classFields = new HashMap<>();

    public Class<?> getClass(String className) {
        if (classes.containsKey(className)) {
            return classes.get(className);
        }

        Class<?> obtainedClass = null;

        try {
            obtainedClass = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            MessageUtil.printException(ex);
        } finally {
            classes.put(className, obtainedClass);
        }
        return obtainedClass;
    }

    private Object getValue(@NotNull Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        Object value = field.get(object);
        field.setAccessible(accessible);
        return value;
    }

    public Object getField(Object object, Class<?> fieldType, int number) throws IllegalAccessException {
        if (object == null) {
            throw new IllegalAccessException("Tried to access field from a null object");
        }

        Class<?> objectClass = object.getClass();
        Map<Class<?>, Map<Integer, Field>> typeFields = classFields.getOrDefault(objectClass, new HashMap<>());
        Map<Integer, Field> fields = typeFields.getOrDefault(fieldType, new HashMap<>());

        classFields.put(objectClass, typeFields);
        typeFields.put(fieldType, fields);

        if (!fields.isEmpty() && fields.containsKey(number)) {
            return getValue(fields.get(number), object);
        }

        int index = 0;

        for (Field field : objectClass.getFields()) {
            if (fieldType == field.getType() && index++ >= number) {
                Object value = getValue(field, object);
                fields.put(number, field);
                return value;
            }
        }
        return null;
    }

    public Object getField(Object object, Class<?> fieldType) throws IllegalAccessException {
        return getField(object, fieldType, 0);
    }

    private Class<?> getMinecraftClass(@NotNull String key) {
        int lastDot = key.lastIndexOf(".");
        String lastKey = key.substring(lastDot > 0 ? lastDot + 1 : 0);
        Class<?> legacyClass = getClass("net.minecraft.server." + version + "." + lastKey);
        Class<?> newClass = getClass("net.minecraft." + key);
        return legacyClass != null ? legacyClass : newClass;
    }

    private Class<?> getCraftBukkitClass(@NotNull String key) {
        int lastDot = key.lastIndexOf(".");
        String lastKey = key.substring(lastDot > 0 ? lastDot + 1 : 0);
        Class<?> legacyClass = getClass("org.bukkit.craftbukkit." + version + "." + lastKey);
        Class<?> newClass = getClass("org.bukkit.craftbukkit." + version + "." + key);
        return legacyClass != null ? legacyClass : newClass;
    }

    public Class<?> getItemStack() {
        return getMinecraftClass("world.item.ItemStack");
    }

    public Class<?> getMinecraftKey() {
        return getMinecraftClass("resources.MinecraftKey");
    }

    public Class<?> getEnumProtocol() {
        return getMinecraftClass("network.EnumProtocol");
    }

    public Class<?> getEnumProtocolDirection() {
        return getMinecraftClass("network.protocol.EnumProtocolDirection");
    }

    public Class<?> getNetworkManager() {
        return getMinecraftClass("network.NetworkManager");
    }

    public Class<?> getPacketDataSerializer() {
        return getMinecraftClass("network.PacketDataSerializer");
    }

    public Class<?> getPacket() {
        return getMinecraftClass("network.protocol.Packet");
    }

    public Class<?> getIChatBaseComponent() {
        return getMinecraftClass("network.chat.IChatBaseComponent");
    }

    public Class<?> getPacketPlayOutKickDisconnect() {
        return getMinecraftClass("network.protocol.game.PacketPlayOutKickDisconnect");
    }

    public Class<?> getPacketPlayOutTitle() {
        return getMinecraftClass("network.protocol.game.PacketPlayOutTitle");
    }

    public Class<?> getPacketPlayOutChat() {
        return getMinecraftClass("network.protocol.game.PacketPlayOutChat");
    }

    public Class<?> getPlayerConnection() {
        return getMinecraftClass("server.network.PlayerConnection");
    }

    public Class<?> getChatMessageType() {
        return getMinecraftClass("network.chat.ChatMessageType");
    }

    public Class<?> getCraftItemStack() {
        return getCraftBukkitClass("inventory.CraftItemStack");
    }
}
