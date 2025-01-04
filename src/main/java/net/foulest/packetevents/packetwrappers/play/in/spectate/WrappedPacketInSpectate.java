package net.foulest.packetevents.packetwrappers.play.in.spectate;

import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;

import java.util.UUID;

public class WrappedPacketInSpectate extends WrappedPacket {

    public WrappedPacketInSpectate(NMSPacket packet) {
        super(packet);
    }

    public UUID getUUID() {
        return readObject(0, UUID.class);
    }

    public void setUUID(UUID uuid) {
        writeObject(0, uuid);
    }
}
