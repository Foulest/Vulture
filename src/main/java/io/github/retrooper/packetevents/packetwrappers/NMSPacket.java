package io.github.retrooper.packetevents.packetwrappers;

import io.github.retrooper.packetevents.utils.reflection.ClassUtil;
import lombok.Getter;

@Getter
public class NMSPacket {

    private final Object rawNMSPacket;

    public NMSPacket(Object rawNMSPacket) {
        this.rawNMSPacket = rawNMSPacket;
    }

    public String getName() {
        return ClassUtil.getClassSimpleName(rawNMSPacket.getClass());
    }
}
