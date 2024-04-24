package io.github.retrooper.packetevents.packetwrappers.play.out.entitydestroy;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;

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
        if (entityID == -1 && entityIds.length == 0 && packet != null) {
            entityIds = readIntArray(0);
        }
        return entityIds[0];
    }

    @Override
    public void setEntityId(int entityID) {
        if (packet != null) {
            entityIds = new int[]{entityID};
            writeIntArray(0, new int[]{entityIds[0]});
        } else {
            entityIds = new int[]{entityID};
        }

        entity = null;
    }

    public int[] getEntityIds() {
        if (packet != null) {
            return readIntArray(0);
        } else {
            return entityIds;
        }
    }

    public void setEntityIds(int... entityIds) {
        if (packet != null) {
            writeIntArray(0, entityIds);
        } else {
            this.entityIds = entityIds;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(getEntityIds());
    }
}
