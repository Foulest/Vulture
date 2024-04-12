package io.github.retrooper.packetevents.packetwrappers.play.out.mount;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;

// TODO: Test sending this wrapper on 1.17
public class WrappedPacketOutMount extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static boolean v_1_17;
    private static Constructor<?> packetConstructor;
    private int[] passengerIDs;

    public WrappedPacketOutMount(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutMount(int entityID, int[] passengerIDs) {
        setEntityId(entityID);
        this.passengerIDs = passengerIDs;
    }

    public WrappedPacketOutMount(Entity entity, int[] passengerIDs) {
        setEntity(entity);
        this.passengerIDs = passengerIDs;
    }

    @Override
    protected void load() {
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);

        try {
            if (v_1_17) {
                packetConstructor = PacketTypeClasses.Play.Server.MOUNT.getConstructor(NMSUtils.packetDataSerializerClass);
            } else {
                packetConstructor = PacketTypeClasses.Play.Server.MOUNT.getConstructor();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public int[] getPassengerIds() {
        if (packet != null) {
            return readIntArray(0);
        } else {
            return passengerIDs;
        }
    }

    public void setPassengerIds(int[] passengerIDs) {
        if (packet != null) {
            writeIntArray(0, passengerIDs);
        } else {
            this.passengerIDs = passengerIDs;
        }
    }

    @Override
    public boolean isSupported() {
        return version.isNewerThan(ServerVersion.v_1_8_8);
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object packetInstance;

        if (v_1_17) {
            Object byteBuf = PacketEvents.get().getByteBufUtil().newByteBuf(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
            Object packetDataSerializer = NMSUtils.generatePacketDataSerializer(byteBuf);
            packetInstance = packetConstructor.newInstance(packetDataSerializer);
        } else {
            packetInstance = packetConstructor.newInstance();
        }

        WrappedPacketOutMount wrappedPacketOutMount = new WrappedPacketOutMount(new NMSPacket(packetInstance));
        wrappedPacketOutMount.setEntityId(getEntityId());
        wrappedPacketOutMount.setPassengerIds(getPassengerIds());
        return packetInstance;
    }
}
