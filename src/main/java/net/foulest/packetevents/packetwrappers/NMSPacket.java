package net.foulest.packetevents.packetwrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.foulest.packetevents.utils.reflection.ClassUtil;

@Getter
@AllArgsConstructor
public class NMSPacket {

    private final Object rawNMSPacket;

    public String getName() {
        return ClassUtil.getClassSimpleName(rawNMSPacket.getClass());
    }
}
