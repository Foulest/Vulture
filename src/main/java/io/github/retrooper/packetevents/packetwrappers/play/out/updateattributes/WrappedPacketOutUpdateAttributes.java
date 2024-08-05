package io.github.retrooper.packetevents.packetwrappers.play.out.updateattributes;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.attributesnapshot.AttributeSnapshotWrapper;
import lombok.ToString;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ToString
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
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private List<AttributeSnapshotWrapper> getProperties() {
        if (nmsPacket != null) {
            List<Object> list = readList();
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
        if (nmsPacket != null) {
            List<Object> list = new ArrayList<>(properties.size());

            for (AttributeSnapshotWrapper attributeSnapshotWrapper : properties) {
                if (attributeSnapshotWrapper.getNMSPacket() != null) {
                    list.add(attributeSnapshotWrapper.getNMSPacket().getRawNMSPacket());
                }
            }

            writeList(list);
        } else {
            this.properties = properties;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        List<AttributeSnapshotWrapper> wrapperList = getProperties();
        List<Object> nmsProperties = new ArrayList<>(wrapperList.size());

        for (AttributeSnapshotWrapper property : wrapperList) {
            if (property.getNMSPacket() != null) {
                nmsProperties.add(property.getNMSPacket().getRawNMSPacket());
            }
        }
        return packetConstructor.newInstance(getEntityId(), nmsProperties);
    }
}
