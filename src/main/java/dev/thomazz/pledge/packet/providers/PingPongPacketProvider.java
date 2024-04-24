package dev.thomazz.pledge.packet.providers;

import dev.thomazz.pledge.packet.PingPacketProvider;
import dev.thomazz.pledge.util.MinecraftReflection;
import dev.thomazz.pledge.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class PingPongPacketProvider implements PingPacketProvider {

    private final Class<?> pongClass;
    private final Field pongIdField;
    private final Constructor<?> pingConstructor;

    public PingPongPacketProvider() throws Exception {
        pongClass = MinecraftReflection.gamePacket("ServerboundPongPacket");
        pongIdField = ReflectionUtil.getFieldByType(pongClass, int.class);

        Class<?> pingClass = MinecraftReflection.gamePacket("ClientboundPingPacket");
        pingConstructor = pingClass.getConstructor(int.class);
    }

    @Override
    public Object buildPacket(int id) throws Exception {
        return pingConstructor.newInstance(id);
    }

    @Override
    public int idFromPong(Object packet) throws Exception {
        return pongIdField.getInt(packet);
    }

    @Override
    public boolean isPong(Object packet) {
        return pongClass.isInstance(packet);
    }

    @Override
    public int getLowerBound() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getUpperBound() {
        return Integer.MAX_VALUE;
    }
}
