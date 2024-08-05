package io.github.retrooper.packetevents.packetwrappers.play.out.collect;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
@AllArgsConstructor
public class WrappedPacketOutCollect extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private int collectedEntityId;
    private int collectorEntityId;

    public WrappedPacketOutCollect(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.COLLECT.getConstructor(int.class, int.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private int getCollectedEntityId() {
        if (nmsPacket != null) {
            return readInt(0);
        } else {
            return collectedEntityId;
        }
    }

    public void setCollectedEntityId(int id) {
        if (nmsPacket != null) {
            writeInt(0, id);
        } else {
            collectedEntityId = id;
        }
    }

    private int getCollectorEntityId() {
        if (nmsPacket != null) {
            return readInt(1);
        } else {
            return collectorEntityId;
        }
    }

    public void setCollectorEntityId(int id) {
        if (nmsPacket != null) {
            writeInt(1, id);
        } else {
            collectorEntityId = id;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return packetConstructor.newInstance(getCollectedEntityId(), getCollectorEntityId());
    }
}
