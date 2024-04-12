package io.github.retrooper.packetevents.packetwrappers.login.out.encryptionbegin;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import org.jetbrains.annotations.Nullable;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class WrappedPacketLoginOutEncryptionBegin extends WrappedPacket {

    private static boolean v_1_17;

    public WrappedPacketLoginOutEncryptionBegin(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);
    }

    public String getEncodedString() {
        return readString(0);
    }

    public void setEncodedString(String encodedString) {
        writeString(0, encodedString);
    }

    public PublicKey getPublicKey() {
        return v_1_17 ? encrypt(readByteArray(0)) : readObject(0, PublicKey.class);
    }

    public void setPublicKey(PublicKey key) {
        if (v_1_17) {
            writeByteArray(0, key.getEncoded());
        } else {
            writeObject(0, key);
        }
    }

    public byte[] getVerifyToken() {
        return readByteArray(v_1_17 ? 1 : 0);
    }

    public void setVerifyToken(byte[] verifyToken) {
        writeByteArray(v_1_17 ? 1 : 0, verifyToken);
    }

    private @Nullable PublicKey encrypt(byte[] bytes) {
        try {
            EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isSupported() {
        return PacketTypeClasses.Login.Server.ENCRYPTION_BEGIN != null;
    }
}
