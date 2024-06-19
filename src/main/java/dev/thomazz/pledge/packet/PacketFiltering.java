/*
 * This file is part of Pledge - https://github.com/ThomasOM/Pledge
 * Copyright (C) 2021 Thomazz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
