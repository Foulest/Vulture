package io.github.retrooper.packetevents.packetwrappers;

import io.github.retrooper.packetevents.utils.reflection.ClassUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NMSPacket {

    private final Object rawNMSPacket;

    public String getName() {
        return ClassUtil.getClassSimpleName(rawNMSPacket.getClass());
    }
}
