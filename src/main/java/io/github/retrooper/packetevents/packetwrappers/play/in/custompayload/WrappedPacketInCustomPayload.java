package io.github.retrooper.packetevents.packetwrappers.play.in.custompayload;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketReader;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.Reflection;

public final class WrappedPacketInCustomPayload extends WrappedPacket {

    private static boolean strPresent;
    private static boolean byteArrayPresent;

    public WrappedPacketInCustomPayload(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        strPresent = Reflection.getField(PacketTypeClasses.Play.Client.CUSTOM_PAYLOAD, String.class, 0) != null;
        byteArrayPresent = Reflection.getField(PacketTypeClasses.Play.Client.CUSTOM_PAYLOAD, byte[].class, 0) != null;
    }

    public String getChannelName() {
        if (strPresent) {
            return readString(0);
        } else {
            return readMinecraftKey(1);
        }
    }

    public void setChannelName(String channelName) {
        if (strPresent) {
            writeString(0, channelName);
        } else {
            writeMinecraftKey(1, channelName);
        }
    }

    public byte[] getData() {
        if (byteArrayPresent) {
            return readByteArray(0);
        } else {
            return PacketEvents.getInstance().getByteBufUtil().getBytes(getBuffer());
        }
    }

    public void setData(byte[] data) {
        if (byteArrayPresent) {
            writeByteArray(0, data);
        } else {
            PacketEvents.getInstance().getByteBufUtil().setBytes(getBuffer(), data);
        }
    }

    private Object getBuffer() {
        Object dataSerializer = readObject(0, NMSUtils.packetDataSerializerClass);
        WrapperPacketReader dataSerializerWrapper = new WrappedPacket(new NMSPacket(dataSerializer));
        return dataSerializerWrapper.readObject(0, NMSUtils.byteBufClass);
    }

    public void retain() {
        if (nmsPacket != null && !byteArrayPresent) {
            PacketEvents.getInstance().getByteBufUtil().retain(getBuffer());
        }
    }

    public void release() {
        if (nmsPacket != null && !byteArrayPresent) {
            PacketEvents.getInstance().getByteBufUtil().release(getBuffer());
        }
    }
}
