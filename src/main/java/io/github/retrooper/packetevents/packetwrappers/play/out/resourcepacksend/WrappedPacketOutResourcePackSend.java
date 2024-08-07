package io.github.retrooper.packetevents.packetwrappers.play.out.resourcepacksend;

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
public class WrappedPacketOutResourcePackSend extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private String url;
    private String hash;

    public WrappedPacketOutResourcePackSend(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Play.Server.RESOURCE_PACK_SEND.getConstructor(String.class, String.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public String getUrl() {
        if (nmsPacket != null) {
            return readString(0);
        }
        return url;
    }

    public void setUrl(String url) {
        if (nmsPacket != null) {
            writeString(0, url);
        } else {
            this.url = url;
        }
    }

    private String getHash() {
        if (nmsPacket != null) {
            return readString(1);
        }
        return hash;
    }

    public void setHash(String hash) {
        if (nmsPacket != null) {
            writeString(1, hash);
        } else {
            this.hash = hash;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return packetConstructor.newInstance(getUrl(), getHash());
    }
}
