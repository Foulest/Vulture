package io.github.retrooper.packetevents.packetwrappers.play.out.entitydestroy;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import lombok.ToString;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
public class WrappedPacketOutEntityDestroy extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int[] entityIds = new int[0];

    public WrappedPacketOutEntityDestroy(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutEntityDestroy(int entityID) {
        setEntityId(entityID);
    }

    public WrappedPacketOutEntityDestroy(Entity entity) {
        setEntity(entity);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.ENTITY_DESTROY.getConstructor(int[].class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getEntityId() {
        if (entityID == -1 && entityIds.length == 0 && nmsPacket != null) {
            entityIds = readIntArray(0);
        }
        return entityIds[0];
    }

    @Override
    public void setEntityId(int entityID) {
        if (nmsPacket != null) {
            entityIds = new int[]{entityID};
            writeIntArray(0, new int[]{entityIds[0]});
        } else {
            entityIds = new int[]{entityID};
        }

        entity = null;
    }

    public int[] getEntityIds() {
        if (nmsPacket != null) {
            return readIntArray(0);
        } else {
            return entityIds;
        }
    }

    public void setEntityIds(int... entityIds) {
        if (nmsPacket != null) {
            writeIntArray(0, entityIds);
        } else {
            this.entityIds = entityIds;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return packetConstructor.newInstance(getEntityIds());
    }
}
