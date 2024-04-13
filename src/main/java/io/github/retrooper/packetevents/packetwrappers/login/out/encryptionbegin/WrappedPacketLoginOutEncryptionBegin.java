package io.github.retrooper.packetevents.packetwrappers.login.out.encryptionbegin;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import org.jetbrains.annotations.Nullable;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class WrappedPacketLoginOutEncryptionBegin extends WrappedPacket {

    public WrappedPacketLoginOutEncryptionBegin(NMSPacket packet) {
        super(packet);
    }

    public String getEncodedString() {
        return readString(0);
    }

    public void setEncodedString(String encodedString) {
        writeString(0, encodedString);
    }

    public PublicKey getPublicKey() {
        return readObject(0, PublicKey.class);
    }

    public void setPublicKey(PublicKey key) {
        writeObject(0, key);
    }

    public byte[] getVerifyToken() {
        return readByteArray(0);
    }

    public void setVerifyToken(byte[] verifyToken) {
        writeByteArray(0, verifyToken);
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
}
