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
    protected final @Nullable NMSPacket nmsPacket;
    private final @Nullable Class<?> packetClass;

    protected WrappedPacket() {
        nmsPacket = null;
        packetClass = null;
        load0();
    }

    public WrappedPacket(NMSPacket nmsPacket) {
        this(nmsPacket, nmsPacket.getRawNMSPacket().getClass());
    }

    public WrappedPacket(@Nullable NMSPacket nmsPacket, @NotNull Class<?> packetClass) {
        if (packetClass.getSuperclass().equals(PacketTypeClasses.Play.Client.FLYING)) {
            packetClass = PacketTypeClasses.Play.Client.FLYING;
        } else if (packetClass.getSuperclass().equals(PacketTypeClasses.Play.Server.ENTITY)) {
            packetClass = PacketTypeClasses.Play.Server.ENTITY;
        }

        this.packetClass = packetClass;
        this.nmsPacket = nmsPacket;
        load0();
    }

    private void load0() {
        Class<? extends WrappedPacket> clazz = getClass();

        if (!LOADED_WRAPPERS.containsKey(clazz)) {
            try {
                load();
                LOADED_WRAPPERS.put(clazz, true);
            } catch (RuntimeException ex) {
                String wrapperName = ClassUtil.getClassSimpleName(clazz);
                PacketEvents.getPlugin().getLogger().log(Level.SEVERE, ex, () -> "PacketEvents found an"
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
            if (packetClass == null) {
                throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                        + index + " in the null class!");
            }

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
            if (nmsPacket == null) {
                throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                        + field.getName() + " in the null packet!");
            }
            return field.get(nmsPacket.getRawNMSPacket());
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
            if (nmsPacket == null) {
                throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                        + index + " in the null packet!");
            }

            Field field = getField(type, index);
            return (T) field.get(nmsPacket.getRawNMSPacket());
        } catch (IllegalAccessException | NullPointerException | ArrayIndexOutOfBoundsException e) {
            if (packetClass == null) {
                throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                        + index + " in the null class!");
            } else {
                throw new WrapperFieldNotFoundException(packetClass, type, index);
            }
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
            if (packetClass == null) {
                throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                        + index + " in the null class!");
            }

            if (nmsPacket == null) {
                throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                        + index + " in the null packet!");
            }

            Field field = packetClass.getDeclaredFields()[index];

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            field.set(nmsPacket.getRawNMSPacket(), value);
        } catch (WrapperFieldNotFoundException | IllegalAccessException ex) {
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

    public void write(Class<?> type, int index, Object value) {
        if (packetClass == null) {
            throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                    + index + " in the null class!");
        }

        if (nmsPacket == null) {
            throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                    + index + " in the null packet!");
        }

        Field field = getField(type, index);

        if (field == null) {
            throw new WrapperFieldNotFoundException(packetClass, type, index);
        }

        try {
            field.set(nmsPacket.getRawNMSPacket(), value);
        } catch (IllegalAccessException | NullPointerException ex) {
            ex.printStackTrace();
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

    private static @Nullable Vector3i getVector3i(Object blockPosObj) {
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

    protected ItemStack readItemStack() {
        Object nmsItemStack = readObject(0, NMSUtils.nmsItemStackClass);
        return NMSUtils.toBukkitItemStack(nmsItemStack);
    }

    protected void writeItemStack(ItemStack stack) {
        Object nmsItemStack = NMSUtils.toNMSItemStack(stack);
        write(NMSUtils.nmsItemStackClass, 0, nmsItemStack);
    }

    public @Nullable GameMode readGameMode(int index) {
        Enum<?> enumConst = readEnumConstant(index, NMSUtils.enumGameModeClass);
        int targetIndex = enumConst.ordinal() - 1;

        if (targetIndex == -1) {
            return null;
        }
        return GameMode.values()[targetIndex];
    }

    protected void writeGameMode(@Nullable GameMode gameMode) {
        int i = gameMode != null ? (gameMode.ordinal() + 1) : (0);
        Enum<?> enumConst = EnumUtil.valueByIndex(NMSUtils.enumGameModeClass.asSubclass(Enum.class), i);
        writeEnumConstant(0, enumConst);
    }

    protected World.Environment readDimension() {
        int dimensionID = readInt(0);

        switch (dimensionID) {
            case -1:
                return World.Environment.NETHER;

            case 1:
                return World.Environment.THE_END;

            default:
                return World.Environment.NORMAL;
        }
    }

    protected void writeDimension(World.@NotNull Environment dimension) {
        writeInt(0, dimension.getId());
    }

    protected Difficulty readDifficulty() {
        Enum<?> enumConstant = readEnumConstant(0, NMSUtils.enumDifficultyClass);
        return Difficulty.values()[enumConstant.ordinal()];
    }

    protected void writeDifficulty(@NotNull Difficulty difficulty) {
        Enum<?> enumConstant = EnumUtil.valueByIndex(NMSUtils.enumDifficultyClass.asSubclass(Enum.class), difficulty.ordinal());
        writeEnumConstant(0, enumConstant);
    }

    public String readIChatBaseComponent(int index) {
        Object iChatBaseComponent = readObject(index, NMSUtils.iChatBaseComponentClass);
        return NMSUtils.readIChatBaseComponent(iChatBaseComponent);
    }

    protected void writeIChatBaseComponent(String content) {
        Object iChatBaseComponent = NMSUtils.generateIChatBaseComponent(content);
        write(NMSUtils.iChatBaseComponentClass, 0, iChatBaseComponent);
    }

    public String readMinecraftKey(int index) {
        int namespaceIndex = 0;
        int keyIndex = 1;
        Object minecraftKey = readObject(index, NMSUtils.minecraftKeyClass);
        WrapperPacketReader minecraftKeyWrapper = new WrappedPacket(new NMSPacket(minecraftKey));
        return minecraftKeyWrapper.readString(namespaceIndex) + ":" + minecraftKeyWrapper.readString(keyIndex);
    }

    protected void writeMinecraftKey(int index, String content) {
        Object minecraftKey = null;

        try {
            minecraftKey = NMSUtils.minecraftKeyConstructor.newInstance(content);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }

        write(NMSUtils.minecraftKeyClass, index, minecraftKey);
    }

    @SuppressWarnings("unchecked")
    protected List<Object> readList() {
        return read(0, List.class);
    }

    protected void writeList(List<Object> list) {
        write(List.class, 0, list);
    }

    private Field getField(Class<?> type, int index) {
        if (packetClass == null) {
            throw new WrapperFieldNotFoundException("PacketEvents failed to find any field indexed "
                    + index + " in the null class!");
        }

        Map<Class<?>, Field[]> cached = FIELD_CACHE.computeIfAbsent(packetClass, k -> new ConcurrentHashMap<>());
        Field[] fields = cached.computeIfAbsent(type, typeClass -> getFields(typeClass, packetClass.getDeclaredFields()));

        if (fields.length >= index + 1) {
            return fields[index];
        } else {
            throw new WrapperFieldNotFoundException(packetClass, type, index);
        }
    }

    private static Field @NotNull [] getFields(Class<?> type, Field @NotNull [] fields) {
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
}
