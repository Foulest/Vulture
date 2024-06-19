package io.github.retrooper.packetevents.packetwrappers;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.exceptions.WrapperFieldNotFoundException;
import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketReader;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketWriter;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.ClassUtil;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class WrappedPacket implements WrapperPacketReader, WrapperPacketWriter {

    private static final Map<Class<? extends WrappedPacket>, Boolean> LOADED_WRAPPERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<Class<?>, Field[]>> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];
    public static ServerVersion version;
    protected final NMSPacket packet;
    private final Class<?> packetClass;

    public WrappedPacket() {
        packet = null;
        packetClass = null;
        load0();
    }

    public WrappedPacket(NMSPacket packet) {
        this(packet, packet.getRawNMSPacket().getClass());
    }

    public WrappedPacket(NMSPacket packet, @NotNull Class<?> packetClass) {
        if (packetClass.getSuperclass().equals(PacketTypeClasses.Play.Client.FLYING)) {
            packetClass = PacketTypeClasses.Play.Client.FLYING;
        } else if (packetClass.getSuperclass().equals(PacketTypeClasses.Play.Server.ENTITY)) {
            packetClass = PacketTypeClasses.Play.Server.ENTITY;
        }

        this.packetClass = packetClass;
        this.packet = packet;
        load0();
    }

    private void load0() {
        Class<? extends WrappedPacket> clazz = getClass();

        if (!LOADED_WRAPPERS.containsKey(clazz)) {
            try {
                load();
                LOADED_WRAPPERS.put(clazz, true);
            } catch (Exception ex) {
                String wrapperName = ClassUtil.getClassSimpleName(clazz);
                PacketEvents.get().getPlugin().getLogger().log(Level.SEVERE, ex, () -> "PacketEvents found an"
                        + " exception while loading the " + wrapperName + " packet wrapper. Please report this bug!"
                        + " Tell us about your server version, spigot and code (of you using the wrapper)");
                LOADED_WRAPPERS.put(clazz, false);
            }
        }
    }

    protected void load() {
        // Do nothing
    }

    protected boolean hasLoaded() {
        return LOADED_WRAPPERS.getOrDefault(getClass(), false);
    }

    @Override
    public boolean readBoolean(int index) {
        return read(index, boolean.class);
    }

    @Override
    public byte readByte(int index) {
        return read(index, byte.class);
    }

    @Override
    public short readShort(int index) {
        return read(index, short.class);
    }

    @Override
    public int readInt(int index) {
        return read(index, int.class);
    }

    @Override
    public long readLong(int index) {
        return read(index, long.class);
    }

    @Override
    public float readFloat(int index) {
        return read(index, float.class);
    }

    @Override
    public double readDouble(int index) {
        return read(index, double.class);
    }

    @Override
    public boolean[] readBooleanArray(int index) {
        return read(index, boolean[].class);
    }

    @Override
    public byte[] readByteArray(int index) {
        return read(index, byte[].class);
    }

    @Override
    public short[] readShortArray(int index) {
        return read(index, short[].class);
    }

    @Override
    public int[] readIntArray(int index) {
        return read(index, int[].class);
    }

    @Override
    public long[] readLongArray(int index) {
        return read(index, long[].class);
    }

    @Override
    public float[] readFloatArray(int index) {
        return read(index, float[].class);
    }

    @Override
    public double[] readDoubleArray(int index) {
        return read(index, double[].class);
    }

    @Override
    public String[] readStringArray(int index) {
        return read(index, String[].class);
    }

    @Override
    public String readString(int index) {
        return read(index, String.class);
    }

    @Override
    public Object readAnyObject(int index) {
        try {
            Field field = packetClass.getDeclaredFields()[index];

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return getFieldObject(field);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                    + index + " in the " + ClassUtil.getClassSimpleName(packetClass) + " class!");
        }
    }

    private @Nullable Object getFieldObject(Field field) {
        try {
            return field.get(packet.getRawNMSPacket());
        } catch (IllegalAccessException | NullPointerException | ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public <T> T readObject(int index, Class<? extends T> type) {
        return read(index, type);
    }

    @Override
    public Enum<?> readEnumConstant(int index, Class<? extends Enum<?>> type) {
        return read(index, type);
    }

    @SuppressWarnings("unchecked")
    public <T> T read(int index, Class<? extends T> type) {
        try {
            Field field = getField(type, index);
            return (T) field.get(packet.getRawNMSPacket());
        } catch (IllegalAccessException | NullPointerException | ArrayIndexOutOfBoundsException e) {
            throw new WrapperFieldNotFoundException(packetClass, type, index);
        }
    }

    @Override
    public void writeBoolean(int index, boolean value) {
        write(boolean.class, index, value);
    }

    @Override
    public void writeByte(int index, byte value) {
        write(byte.class, index, value);
    }

    @Override
    public void writeShort(int index, short value) {
        write(short.class, index, value);
    }

    @Override
    public void writeInt(int index, int value) {
        write(int.class, index, value);
    }

    @Override
    public void writeLong(int index, long value) {
        write(long.class, index, value);
    }

    @Override
    public void writeFloat(int index, float value) {
        write(float.class, index, value);
    }

    @Override
    public void writeDouble(int index, double value) {
        write(double.class, index, value);
    }

    @Override
    public void writeString(int index, String value) {
        write(String.class, index, value);
    }

    @Override
    public void writeObject(int index, Object value) {
        write(value.getClass(), index, value);
    }

    @Override
    public void writeBooleanArray(int index, boolean[] array) {
        write(boolean[].class, index, array);
    }

    @Override
    public void writeByteArray(int index, byte[] value) {
        write(byte[].class, index, value);
    }

    @Override
    public void writeShortArray(int index, short[] value) {
        write(short[].class, index, value);
    }

    @Override
    public void writeIntArray(int index, int[] value) {
        write(int[].class, index, value);
    }

    @Override
    public void writeLongArray(int index, long[] value) {
        write(long[].class, index, value);
    }

    @Override
    public void writeFloatArray(int index, float[] value) {
        write(float[].class, index, value);
    }

    @Override
    public void writeDoubleArray(int index, double[] value) {
        write(double[].class, index, value);
    }

    @Override
    public void writeStringArray(int index, String[] value) {
        write(String[].class, index, value);
    }

    @Override
    public void writeAnyObject(int index, Object value) {
        try {
            Field field = packetClass.getDeclaredFields()[index];

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            field.set(packet.getRawNMSPacket(), value);
        } catch (Exception e) {
            throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                    + index + " in the " + ClassUtil.getClassSimpleName(packetClass) + " class!");
        }
    }

    @Override
    public void writeEnumConstant(int index, Enum<?> enumConstant) {
        try {
            write(enumConstant.getClass(), index, enumConstant);
        } catch (WrapperFieldNotFoundException ex) {
            write(enumConstant.getDeclaringClass(), index, enumConstant);
        }
    }

    public void write(Class<?> type, int index, Object value) throws WrapperFieldNotFoundException {
        Field field = getField(type, index);

        if (field == null) {
            throw new WrapperFieldNotFoundException(packetClass, type, index);
        }

        try {
            field.set(packet.getRawNMSPacket(), value);
        } catch (IllegalAccessException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public Vector3i readBlockPosition(int index) {
        Object blockPosObj = readObject(index, NMSUtils.blockPosClass);
        return getVector3i(blockPosObj);
    }

    public Vector3i readSectionPosition(int index) {
        Object blockPosObj = readObject(index, NMSUtils.sectionPositionClass);
        return getVector3i(blockPosObj);
    }

    private @Nullable Vector3i getVector3i(Object blockPosObj) {
        try {
            int x = (Integer) NMSUtils.getBlockPosX.invoke(blockPosObj);
            int y = (Integer) NMSUtils.getBlockPosY.invoke(blockPosObj);
            int z = (Integer) NMSUtils.getBlockPosZ.invoke(blockPosObj);
            return new Vector3i(x, y, z);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void writeBlockPosition(int index, Vector3i blockPosition) {
        Object blockPosObj = NMSUtils.generateNMSBlockPos(blockPosition);
        write(NMSUtils.blockPosClass, index, blockPosObj);
    }

    public ItemStack readItemStack(int index) {
        Object nmsItemStack = readObject(index, NMSUtils.nmsItemStackClass);
        return NMSUtils.toBukkitItemStack(nmsItemStack);
    }

    public void writeItemStack(int index, ItemStack stack) {
        Object nmsItemStack = NMSUtils.toNMSItemStack(stack);
        write(NMSUtils.nmsItemStackClass, index, nmsItemStack);
    }

    public GameMode readGameMode(int index) {
        Enum<?> enumConst = readEnumConstant(index, NMSUtils.enumGameModeClass);
        int targetIndex = enumConst.ordinal() - 1;

        if (targetIndex == -1) {
            return null;
        }
        return GameMode.values()[targetIndex];
    }

    public void writeGameMode(int index, @Nullable GameMode gameMode) {
        int i = gameMode != null ? (gameMode.ordinal() + 1) : (0);
        Enum<?> enumConst = EnumUtil.valueByIndex(NMSUtils.enumGameModeClass.asSubclass(Enum.class), i);
        writeEnumConstant(index, enumConst);
    }

    public World.Environment readDimension(int dimensionIDLegacyIndex) {
        int dimensionID = readInt(dimensionIDLegacyIndex);

        switch (dimensionID) {
            case -1:
                return World.Environment.NETHER;

            case 1:
                return World.Environment.THE_END;

            default:
                return World.Environment.NORMAL;
        }
    }

    public void writeDimension(int dimensionIDLegacyIndex, World.@NotNull Environment dimension) {
        writeInt(dimensionIDLegacyIndex, dimension.getId());
    }

    public Difficulty readDifficulty(int index) {
        Enum<?> enumConstant = readEnumConstant(index, NMSUtils.enumDifficultyClass);
        return Difficulty.values()[enumConstant.ordinal()];
    }

    public void writeDifficulty(int index, @NotNull Difficulty difficulty) {
        Enum<?> enumConstant = EnumUtil.valueByIndex(NMSUtils.enumDifficultyClass.asSubclass(Enum.class), difficulty.ordinal());
        writeEnumConstant(index, enumConstant);
    }

    public String readIChatBaseComponent(int index) {
        Object iChatBaseComponent = readObject(index, NMSUtils.iChatBaseComponentClass);
        return NMSUtils.readIChatBaseComponent(iChatBaseComponent);
    }

    public void writeIChatBaseComponent(int index, String content) {
        Object iChatBaseComponent = NMSUtils.generateIChatBaseComponent(content);
        write(NMSUtils.iChatBaseComponentClass, index, iChatBaseComponent);
    }

    public String readMinecraftKey(int index) {
        int namespaceIndex = 0;
        int keyIndex = 1;
        Object minecraftKey = readObject(index, NMSUtils.minecraftKeyClass);
        WrappedPacket minecraftKeyWrapper = new WrappedPacket(new NMSPacket(minecraftKey));
        return minecraftKeyWrapper.readString(namespaceIndex) + ":" + minecraftKeyWrapper.readString(keyIndex);
    }

    public void writeMinecraftKey(int index, String content) {
        Object minecraftKey = null;

        try {
            minecraftKey = NMSUtils.minecraftKeyConstructor.newInstance(content);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        write(NMSUtils.minecraftKeyClass, index, minecraftKey);
    }

    public List<Object> readList(int index) {
        return read(index, List.class);
    }

    public void writeList(int index, List<Object> list) {
        write(List.class, index, list);
    }

    private Field getField(Class<?> type, int index) {
        Map<Class<?>, Field[]> cached = FIELD_CACHE.computeIfAbsent(packetClass, k -> new ConcurrentHashMap<>());
        Field[] fields = cached.computeIfAbsent(type, typeClass -> getFields(typeClass, packetClass.getDeclaredFields()));

        if (fields.length >= index + 1) {
            return fields[index];
        } else {
            throw new WrapperFieldNotFoundException(packetClass, type, index);
        }
    }

    private Field @NotNull [] getFields(Class<?> type, Field @NotNull [] fields) {
        List<Field> ret = new ArrayList<>();

        for (Field field : fields) {
            if (field.getType().equals(type)) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                ret.add(field);
            }
        }
        return ret.toArray(EMPTY_FIELD_ARRAY);
    }

    /**
     * Does the local server version support reading at-least one field with this packet wrapper?
     * If it does, we can label this wrapper to be supported on the local server version.
     * One example where it would not be supported would be if the packet the wrapper is wrapping doesn't even exist on the local server version.
     *
     * @return Is the wrapper supported on the local server version?
     */
    public boolean isSupported() {
        return true;
    }
}
