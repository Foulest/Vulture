package io.github.retrooper.packetevents.packetwrappers.login.out.disconnect;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import lombok.AllArgsConstructor;

import java.lang.reflect.Constructor;

@AllArgsConstructor
public class WrappedPacketLoginOutDisconnect extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private String reason;

    public WrappedPacketLoginOutDisconnect(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        try {
            packetConstructor = PacketTypeClasses.Login.Server.DISCONNECT.getConstructor(NMSUtils.iChatBaseComponentClass);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public String getReason() {
        if (packet != null) {
            return readIChatBaseComponent(0);
        } else {
            return this.reason;
        }
    }

    public void setReason(String reason) {
        if (packet != null) {
            writeIChatBaseComponent(0, reason);
        } else {
            this.reason = reason;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
        return packetConstructor.newInstance(NMSUtils.generateIChatBaseComponent(getReason()));
    }
}
