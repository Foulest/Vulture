package io.github.retrooper.packetevents.packetwrappers.play.out.entitymetadata;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SteelPhoenix, retrooper
 * @since 1.8
 * TODO: Make sendable, allow modifying watchable objects, test on 1.7.10
 */
class WrappedPacketOutEntityMetadata extends WrappedPacketEntityAbstraction {

    WrappedPacketOutEntityMetadata(NMSPacket packet) {
        super(packet);
    }

    public List<WrappedWatchableObject> getWatchableObjects() {
        List<Object> nmsWatchableObjectList = readList();

        // It's annotated as nullable on 1.17 NMS, so lets just handle it being null
        if (nmsWatchableObjectList == null) {
            return new ArrayList<>();
        }

        List<WrappedWatchableObject> wrappedWatchableObjects = new ArrayList<>(nmsWatchableObjectList.size());

        for (Object watchableObject : nmsWatchableObjectList) {
            wrappedWatchableObjects.add(new WrappedWatchableObject(new NMSPacket(watchableObject)));
        }
        return wrappedWatchableObjects;
    }
}
