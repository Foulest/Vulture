package io.github.retrooper.packetevents.packetwrappers.login.in.encryptionbegin;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

public class WrappedPacketLoginInEncryptionBegin extends WrappedPacket {

    public WrappedPacketLoginInEncryptionBegin(NMSPacket packet) {
        super(packet);
    }

    public byte[] getPublicKey() {
        return readByteArray(0);
    }

    public void setPublicKey(byte[] key) {
        writeByteArray(0, key);
    }

    public byte[] getVerifyToken() {
        return readByteArray(1);
    }

    public void setVerifyToken(byte[] token) {
        writeByteArray(1, token);
    }

    @Override
    public boolean isSupported() {
        return PacketTypeClasses.Login.Client.ENCRYPTION_BEGIN != null;
    }
}
