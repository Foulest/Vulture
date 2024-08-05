package io.github.retrooper.packetevents.packetwrappers.play.out.camera;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketWriter;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class WrappedPacketOutCamera extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;

    public WrappedPacketOutCamera(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutCamera(int entityID) {
        setEntityId(entityID);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.CAMERA.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object packetInstance = packetConstructor.newInstance();
        WrapperPacketWriter packetWrapper = new WrappedPacket(new NMSPacket(packetInstance));
        packetWrapper.writeInt(0, getEntityId());
        return packetInstance;
    }
}
