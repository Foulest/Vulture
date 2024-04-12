package io.github.retrooper.packetevents.packetwrappers.login.in.custompayload;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

public class WrappedPacketLoginInCustomPayload extends WrappedPacket {

    private static boolean v_1_17;

    public WrappedPacketLoginInCustomPayload(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);
    }

    public int getMessageId() {
        return readInt(v_1_17 ? 1 : 0);
    }

    public void setMessageId(int id) {
        writeInt(v_1_17 ? 1 : 0, id);
    }

    public byte[] getData() {
        return PacketEvents.get().getByteBufUtil().getBytes(getBuffer());
    }

    public void setData(byte[] data) {
        PacketEvents.get().getByteBufUtil().setBytes(getBuffer(), data);
    }

    private Object getBuffer() {
        Object dataSerializer = readObject(0, NMSUtils.packetDataSerializerClass);
        WrappedPacket byteBufWrapper = new WrappedPacket(new NMSPacket(dataSerializer));
        return byteBufWrapper.readObject(0, NMSUtils.byteBufClass);
    }

    public void retain() {
        PacketEvents.get().getByteBufUtil().retain(getBuffer());
    }

    public void release() {
        PacketEvents.get().getByteBufUtil().release(getBuffer());
    }

    @Override
    public boolean isSupported() {
        return version.isNewerThan(ServerVersion.v_1_12_2);
    }
}
