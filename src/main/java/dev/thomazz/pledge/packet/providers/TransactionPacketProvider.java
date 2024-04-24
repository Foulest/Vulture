package dev.thomazz.pledge.packet.providers;

import dev.thomazz.pledge.packet.PingPacketProvider;
import dev.thomazz.pledge.util.MinecraftReflection;
import dev.thomazz.pledge.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class TransactionPacketProvider implements PingPacketProvider {

    private final Class<?> inTransactionClass;
    private final Field inTransactionIdField;
    private final Constructor<?> outTransactionConstructor;

    public TransactionPacketProvider() throws Exception {
        inTransactionClass = MinecraftReflection.gamePacket("PacketPlayInTransaction");
        inTransactionIdField = ReflectionUtil.getFieldByType(inTransactionClass, short.class);

        Class<?> outTransactionClass = MinecraftReflection.gamePacket("PacketPlayOutTransaction");
        outTransactionConstructor = outTransactionClass.getConstructor(int.class, short.class, boolean.class);
    }

    @Override
    public Object buildPacket(int id) throws Exception {
        return outTransactionConstructor.newInstance(0, (short) id, false);
    }

    @Override
    public int idFromPong(Object packet) throws Exception {
        return inTransactionIdField.getShort(packet);
    }

    @Override
    public boolean isPong(Object packet) throws Exception {
        return inTransactionClass.isInstance(packet) && inTransactionIdField.getShort(packet) < 0;
    }

    @Override
    public int getLowerBound() {
        return Short.MIN_VALUE;
    }

    @Override
    public int getUpperBound() {
        return -1;
    }
}
