package io.github.retrooper.packetevents.packetwrappers.play.out.kickdisconnect;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public final class WrappedPacketOutKickDisconnect extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> kickDisconnectConstructor;
    private String kickMessage;

    public WrappedPacketOutKickDisconnect(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutKickDisconnect(String kickMessage) {
        this.kickMessage = kickMessage;
    }

    @Override
    protected void load() {
        try {
            kickDisconnectConstructor = PacketTypeClasses.Play.Server.KICK_DISCONNECT.getConstructor(NMSUtils.iChatBaseComponentClass);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public String getKickMessage() {
        if (packet != null) {
            Object iChatBaseComponentObject = readObject(0, NMSUtils.iChatBaseComponentClass);
            return NMSUtils.readIChatBaseComponent(iChatBaseComponentObject);
        } else {
            return kickMessage;
        }
    }

    public void setKickMessage(String message) {
        if (packet != null) {
            Object iChatBaseComponent = NMSUtils.generateIChatBaseComponent(message);
            write(NMSUtils.iChatBaseComponentClass, 0, iChatBaseComponent);
        } else {
            kickMessage = message;
        }
    }

    @Override
    public @NotNull Object asNMSPacket() throws Exception {
        return kickDisconnectConstructor.newInstance(NMSUtils.generateIChatBaseComponent(NMSUtils.fromStringToJSON(getKickMessage())));
    }
}
