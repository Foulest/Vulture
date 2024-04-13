package io.github.retrooper.packetevents.packetwrappers.login.out.custompayload;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import lombok.AllArgsConstructor;

import java.lang.reflect.Constructor;

@AllArgsConstructor
public class WrappedPacketLoginOutCustomPayload extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> constructor;
    private static Constructor<?> packetDataSerializerConstructor;
    private int messageID;
    private String channelName;
    private byte[] data;

    public WrappedPacketLoginOutCustomPayload(NMSPacket packet) {
        super(packet);
    }

    public int getMessageId() {
        if (packet != null) {
            return readInt(0);
        }
        return messageID;
    }

    public void setMessageId(int messageID) {
        if (packet != null) {
            writeInt(0, messageID);
        } else {
            this.messageID = messageID;
        }
    }

    public String getChannelName() {
        if (packet != null) {
            return readMinecraftKey(0);
        } else {
            return channelName;
        }
    }

    public void setChannelName(String channelName) {
        if (packet != null) {
            writeMinecraftKey(0, channelName);
        } else {
            this.channelName = channelName;
        }
    }

    public byte[] getData() {
        if (packet != null) {
            Object dataSerializer = readObject(0, NMSUtils.packetDataSerializerClass);
            WrappedPacket byteBufWrapper = new WrappedPacket(new NMSPacket(dataSerializer));
            Object byteBuf = byteBufWrapper.readObject(0, NMSUtils.byteBufClass);
            return PacketEvents.get().getByteBufUtil().getBytes(byteBuf);
        } else {
            return data;
        }
    }

    public void setData(byte[] data) {
        if (packet != null) {
            PacketEvents.get().getByteBufUtil().setBytes(getBuffer(), data);
        } else {
            this.data = data;
        }
    }

    private Object getBuffer() {
        Object dataSerializer = readObject(0, NMSUtils.packetDataSerializerClass);
        WrappedPacket byteBufWrapper = new WrappedPacket(new NMSPacket(dataSerializer));
        return byteBufWrapper.readObject(0, NMSUtils.byteBufClass);
    }

    @Override
    public Object asNMSPacket() throws Exception {
        Object byteBufObject = PacketEvents.get().getByteBufUtil().newByteBuf(data);
        Object minecraftKey = NMSUtils.generateMinecraftKeyNew(channelName);
        Object dataSerializer = packetDataSerializerConstructor.newInstance(byteBufObject);
        return constructor.newInstance(getMessageId(), minecraftKey, dataSerializer);
    }
}
