package io.github.retrooper.packetevents.packetwrappers.play.out.updateattributes;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.attributesnapshot.AttributeSnapshotWrapper;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WrappedPacketOutUpdateAttributes extends WrappedPacketEntityAbstraction implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private List<AttributeSnapshotWrapper> properties;

    public WrappedPacketOutUpdateAttributes(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutUpdateAttributes(int entityID, List<AttributeSnapshotWrapper> properties) {
        this.entityID = entityID;
        this.properties = properties;
    }

    public WrappedPacketOutUpdateAttributes(@NotNull Entity entity, List<AttributeSnapshotWrapper> properties) {
        entityID = entity.getEntityId();
        this.properties = properties;
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.UPDATE_ATTRIBUTES.getConstructor(int.class, Collection.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public List<AttributeSnapshotWrapper> getProperties() {
        if (packet != null) {
            List<Object> list = readList(0);
            List<AttributeSnapshotWrapper> attributeSnapshotWrappers = new ArrayList<>(list.size());

            for (Object nmsAttributeSnapshot : list) {
                attributeSnapshotWrappers.add(new AttributeSnapshotWrapper(new NMSPacket(nmsAttributeSnapshot)));
            }
            return attributeSnapshotWrappers;
        } else {
            return properties;
        }
    }

    public void setProperties(List<AttributeSnapshotWrapper> properties) {
        if (packet != null) {
            List<Object> list = new ArrayList<>(properties.size());

            for (AttributeSnapshotWrapper attributeSnapshotWrapper : properties) {
                list.add(attributeSnapshotWrapper.getNMSPacket().getRawNMSPacket());
            }

            writeList(0, list);
        } else {
            this.properties = properties;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        List<AttributeSnapshotWrapper> properties = getProperties();
        List<Object> nmsProperties = new ArrayList<>(properties.size());

        for (AttributeSnapshotWrapper property : properties) {
            nmsProperties.add(property.getNMSPacket().getRawNMSPacket());
        }
        return packetConstructor.newInstance(getEntityId(), nmsProperties);
    }
}
