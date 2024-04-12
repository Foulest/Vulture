package io.github.retrooper.packetevents.packetwrappers.play.out.camera;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

import java.lang.reflect.Constructor;

public class WrappedPacketOutCamera extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static boolean v_1_17;
    private static Constructor<?> packetConstructor;

    public WrappedPacketOutCamera(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutCamera(int entityID) {
        setEntityId(entityID);
    }

    @Override
    protected void load() {
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);

        try {
            if (v_1_17) {
                packetConstructor = PacketTypeClasses.Play.Server.CAMERA.getConstructor(NMSUtils.packetDataSerializerClass);
            } else {
                packetConstructor = PacketTypeClasses.Play.Server.CAMERA.getConstructor();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object packetInstance;

        if (v_1_17) {
            // You call it lazy, I call it smart
            Object byteBuf = PacketEvents.get().getByteBufUtil().newByteBuf(new byte[]{1});
            Object packetDataSerializer = NMSUtils.generatePacketDataSerializer(byteBuf);
            packetInstance = packetConstructor.newInstance(packetDataSerializer);
        } else {
            packetInstance = packetConstructor.newInstance();
        }

        WrappedPacket packetWrapper = new WrappedPacket(new NMSPacket(packetInstance));
        packetWrapper.writeInt(0, getEntityId());
        return packetInstance;
    }

    @Override
    public boolean isSupported() {
        return version.isNewerThan(ServerVersion.v_1_7_10);
    }
}
