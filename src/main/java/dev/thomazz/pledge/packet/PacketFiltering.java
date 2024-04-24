package dev.thomazz.pledge.packet;

import com.google.common.collect.ImmutableList;
import dev.thomazz.pledge.util.MinecraftReflection;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@UtilityClass
public class PacketFiltering {

    private final List<Class<?>> queueWhiteListPackets = PacketFiltering.buildQueueWhitelistPackets();
    private final List<Class<?>> loginPackets = PacketFiltering.buildLoginPackets();

    private @Unmodifiable List<Class<?>> buildQueueWhitelistPackets() {
        ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();
        PacketFiltering.addGamePacket(builder, "PacketPlayOutKeepAlive");
        PacketFiltering.addGamePacket(builder, "ClientboundKeepAlivePacket");
        PacketFiltering.addGamePacket(builder, "PacketPlayOutKickDisconnect");
        PacketFiltering.addGamePacket(builder, "ClientboundDisconnectPacket");
        PacketFiltering.addGamePacket(builder, "PacketPlayOutChat");
        PacketFiltering.addGamePacket(builder, "ClientboundChatPacket");
        return builder.build();
    }

    private @Unmodifiable List<Class<?>> buildLoginPackets() {
        ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();
        PacketFiltering.addGamePacket(builder, "PacketPlayOutLogin");
        PacketFiltering.addGamePacket(builder, "ClientboundLoginPacket");
        return builder.build();
    }

    private void addGamePacket(ImmutableList.Builder<Class<?>> builder, String packetName) {
        try {
            builder.add(MinecraftReflection.gamePacket(packetName));
        } catch (Exception ignored) {
        }
    }

    // If a packet should be added to the packet queue or instantly sent to players
    public boolean isWhitelistedFromQueue(Object packet) {
        return PacketFiltering.queueWhiteListPackets.stream().anyMatch(type -> type.isInstance(packet));
    }

    // Login packets initiate the game start protocol
    public boolean isLoginPacket(Object packet) {
        return PacketFiltering.loginPackets.stream().anyMatch(type -> type.isInstance(packet));
    }
}
