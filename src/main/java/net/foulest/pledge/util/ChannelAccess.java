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
package net.foulest.pledge.util;

import io.netty.channel.Channel;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@UtilityClass
public class ChannelAccess {

    private final Class<?> NETWORK_MANAGER_CLASS = MinecraftReflection.getMinecraftClass(
            "network.NetworkManager",
            "network.Connection",
            "NetworkManager"
    );

    private final Class<?> PACKET_LISTENER_CLASS = MinecraftReflection.getMinecraftClass(
            "network.PacketListener",
            "PacketListener"
    );

    private final Class<?> PLAYER_CONNECTION_CLASS = MinecraftReflection.getMinecraftClass(
            "server.network.PlayerConnection",
            "server.network.ServerGamePacketListenerImpl",
            "PlayerConnection"
    );

    @SuppressWarnings("OverlyBroadCatchBlock")
    public Channel getChannel(@NotNull Player player) {
        try {
            UUID playerId = player.getUniqueId();
            Object handle = player.getClass().getDeclaredMethod("getHandle").invoke(player);
            Class<?> handleClass = handle.getClass();

            Field playerConnectionField = ReflectionUtil.getFieldByType(handleClass, PLAYER_CONNECTION_CLASS);
            Field channelField = ReflectionUtil.getFieldByType(NETWORK_MANAGER_CLASS, Channel.class);

            // Try the easy way first
            Object playerConnection = playerConnectionField.get(handle);
            if (playerConnection != null) {
                try {
                    Field networkManagerField = ReflectionUtil.getFieldByType(PLAYER_CONNECTION_CLASS, NETWORK_MANAGER_CLASS);
                    Object networkManager = networkManagerField.get(playerConnection);
                    return (Channel) channelField.get(networkManager);
                } catch (NoSuchFieldException ignored) {
                }
            }

            // Try to match all network managers after from game profile
            List<Object> networkManagers = getNetworkManagers();

            for (Object networkManager : networkManagers) {
                Object packetListener = ReflectionUtil.getNonNullFieldByType(networkManager, PACKET_LISTENER_CLASS);

                if (packetListener != null) {
                    Class<?> packetListenerClass = packetListener.getClass();

                    if (packetListenerClass.getSimpleName().equals("LoginListener")
                            || packetListenerClass.getSimpleName().equals("ServerLoginPacketListenerImpl")) {
                        Field profileField = ReflectionUtil.getFieldByClassNames(packetListenerClass, "GameProfile");
                        Object gameProfile = profileField.get(packetListener);

                        // We can use the game profile to look up the player id in the listener
                        Class<?> gameProfileClass = gameProfile.getClass();
                        Field uuidField = ReflectionUtil.getFieldByType(gameProfileClass, UUID.class);
                        UUID foundId = (UUID) uuidField.get(gameProfile);

                        if (playerId.equals(foundId)) {
                            return (Channel) channelField.get(networkManager);
                        }
                    } else {
                        // For player connection listeners we can get the player handle
                        Field playerField;
                        try {
                            playerField = ReflectionUtil.getFieldByClassNames(packetListenerClass, "ServerPlayer", "EntityPlayer");
                        } catch (NoSuchFieldException ignored) {
                            // Might be ServerConfigurationPacketListenerImpl or something else that is unsupported
                            continue;
                        }

                        Object entityPlayer = playerField.get(packetListener);
                        if (handle.equals(entityPlayer)) {
                            return (Channel) channelField.get(networkManager);
                        }
                    }
                }
            }

            throw new NoSuchElementException("Did not find player channel!");
        } catch (IllegalArgumentException | SecurityException
                 | NoSuchElementException | ReflectiveOperationException ex) {
            String playerName = player.getName();
            throw new RuntimeException("Could not get channel for player: " + playerName, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private @NotNull List<Object> getNetworkManagers() {
        try {
            Object serverConnection = MinecraftReflection.getServerConnection();

            for (Field field : serverConnection.getClass().getDeclaredFields()) {
                String typeName = field.getGenericType().getTypeName();
                Class<?> fieldType = field.getType();

                if (!List.class.isAssignableFrom(fieldType)
                        || (!typeName.contains("NetworkManager")
                        && !typeName.contains("Connection"))) {
                    continue;
                }

                field.setAccessible(true);

                List<Object> networkManagers = (List<Object>) field.get(serverConnection);
                return Collections.synchronizedList(networkManagers);
            }

            throw new NoSuchElementException("Did not find correct list in server connection");
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | NoSuchMethodException
                 | SecurityException | InvocationTargetException | NoSuchElementException ex) {
            throw new RuntimeException("Cannot retrieve network managers", ex);
        }
    }
}
