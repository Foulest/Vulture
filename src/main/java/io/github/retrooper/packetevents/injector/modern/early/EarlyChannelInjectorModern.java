/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.retrooper.packetevents.injector.modern.early;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.injector.EarlyInjector;
import io.github.retrooper.packetevents.injector.modern.PlayerChannelHandlerModern;
import io.github.retrooper.packetevents.injector.modern.PlayerDecodeHandlerModern;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.list.ListWrapper;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class EarlyChannelInjectorModern implements EarlyInjector {

    private final Collection<ChannelFuture> injectedFutures = new ArrayList<>();
    private final Collection<Map<Field, Object>> injectedLists = new ArrayList<>();

    @Override
    public boolean isBound() {
        try {
            Object connection = NMSUtils.getMinecraftServerConnection();

            if (connection == null) {
                return false;
            }

            for (Field field : connection.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(connection);

                if (value instanceof List) {
                    synchronized (value) {
                        for (Object object : (Iterable<?>) value) {
                            if (object instanceof ChannelFuture) {
                                return true;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void inject() {
        try {
            Object serverConnection = NMSUtils.getMinecraftServerConnection();

            for (Field field : serverConnection.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = getFieldValue(field, serverConnection);

                if (value instanceof List<?>) {
                    List<?> originalList = (List<?>) value;

                    ListWrapper listWrapper = new ListWrapper(originalList) {
                        @Override
                        public void processAdd(Object object) {
                            if (object instanceof ChannelFuture) {
                                try {
                                    injectChannelFuture((ChannelFuture) object);
                                } catch (RuntimeException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    };

                    Map<Field, Object> map = new HashMap<>();
                    map.put(field, serverConnection);
                    injectedLists.add(map);

                    field.set(serverConnection, listWrapper);

                    synchronized (originalList) {
                        for (Object serverChannel : originalList) {
                            if (serverChannel instanceof ChannelFuture) {
                                injectChannelFuture((ChannelFuture) serverChannel);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("PacketEvents failed to inject!", ex);
        }

        // Player channels might have been registered already. Let us add our handlers. We are a little late though.
        // This only happens when you join extremely early on older versions of minecraft.
        List<Object> networkManagers = NMSUtils.getNetworkManagers();
        synchronized (networkManagers) {
            for (Object networkManager : networkManagers) {
                WrappedPacket networkManagerWrapper = new WrappedPacket(new NMSPacket(networkManager), NMSUtils.networkManagerClass);
                Channel channel = (Channel) networkManagerWrapper.readObject(0, NMSUtils.nettyChannelClass);

                if (channel == null
                        || (!(channel.getClass().equals(NioSocketChannel.class)))
                        && !(channel.getClass().equals(EpollSocketChannel.class))) {
                    continue;
                }

                ChannelPipeline pipeline = channel.pipeline();

                // Remove the old handlers if they exist.
                if (pipeline.get(PacketEvents.getInstance().getHandlerName()) != null) {
                    pipeline.remove(PacketEvents.getInstance().getHandlerName());
                }
                if (pipeline.get(PacketEvents.getInstance().getHandlerName() + "-decoder") != null) {
                    pipeline.remove(PacketEvents.getInstance().getHandlerName() + "-decoder");
                }

                ChannelHandler decodeHandler = new PlayerDecodeHandlerModern();
                ChannelHandler channelHandler = new PlayerChannelHandlerModern();

                // Add the new handlers.
                if (pipeline.get("splitter") != null) {
                    pipeline.addAfter("splitter", PacketEvents.getInstance().getHandlerName() + "-decoder", decodeHandler);
                }
                if (pipeline.get("packet_handler") != null) {
                    pipeline.addBefore("packet_handler", PacketEvents.getInstance().getHandlerName(), channelHandler);
                }
            }
        }
    }

    private static @Nullable Object getFieldValue(@NotNull Field field, Object serverConnection) {
        try {
            return field.get(serverConnection);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void injectChannelFuture(@NotNull ChannelFuture channelFuture) {
        List<String> channelHandlerNames = channelFuture.channel().pipeline().names();
        ChannelHandler bootstrapAcceptor = null;
        Field bootstrapAcceptorField = null;

        for (String handlerName : channelHandlerNames) {
            ChannelHandler handler = channelFuture.channel().pipeline().get(handlerName);

            try {
                bootstrapAcceptorField = handler.getClass().getDeclaredField("childHandler");
                bootstrapAcceptorField.setAccessible(true);
                bootstrapAcceptor = handler;
                break;  // Found the field, no need to continue
            } catch (NoSuchFieldException ex) {
                ex.printStackTrace();
            }
        }

        // Default to first handler if none with childHandler field was found
        if (bootstrapAcceptor == null) {
            bootstrapAcceptor = channelFuture.channel().pipeline().first();
        }

        // Log to check if bootstrapAcceptorField is found and accessible
        if (bootstrapAcceptorField == null) {
            PacketEvents.getPlugin().getLogger().severe("Failed to find the 'childHandler' field in the channel pipeline!");
            throw new IllegalStateException("PacketEvents failed to inject: 'childHandler' field not found!");
        }

        ChannelInitializer<?> oldChannelInitializer;

        try {
            oldChannelInitializer = (ChannelInitializer<?>) bootstrapAcceptorField.get(bootstrapAcceptor);

            if (oldChannelInitializer == null) {
                PacketEvents.getPlugin().getLogger().severe("The 'childHandler' field is null in the channel pipeline!");
                throw new IllegalStateException("PacketEvents failed to inject: 'childHandler' field is null!");
            }

            ChannelInitializer<?> channelInitializer = new PEChannelInitializerModern(oldChannelInitializer);

            // Replace the old channel initializer with our own.
            bootstrapAcceptorField.setAccessible(true);
            bootstrapAcceptorField.set(bootstrapAcceptor, channelInitializer);
            injectedFutures.add(channelFuture);
        } catch (IllegalAccessException ex) {
            ClassLoader cl = bootstrapAcceptor.getClass().getClassLoader();

            if (cl.getClass().getName().equals("org.bukkit.plugin.java.PluginClassLoader")) {
                PluginDescriptionFile yaml = null;

                try {
                    yaml = (PluginDescriptionFile) PluginDescriptionFile.class.getDeclaredField("description").get(cl);
                } catch (IllegalAccessException | NoSuchFieldException e2) {
                    e2.printStackTrace();
                }

                throw new IllegalStateException("PacketEvents failed to inject, because of "
                        + bootstrapAcceptor.getClass().getName() + ", you might want to try running without "
                        + Objects.requireNonNull(yaml).getName() + "?");
            } else {
                throw new IllegalStateException("PacketEvents failed to find core component 'childHandler',"
                        + " please check your plugins. issue: " + bootstrapAcceptor.getClass().getName());
            }
        }
    }

    @Override
    public void eject() {
        // Uninject from players currently online to prevent issues with ProtocolLib.
        for (Player player : PacketEvents.getPlugin().getServer().getOnlinePlayers()) {
            ejectPlayer(player);
        }

        Field childHandlerField = null;

        for (ChannelFuture future : injectedFutures) {
            List<String> names = future.channel().pipeline().names();
            ChannelHandler bootstrapAcceptor = null;

            // Pick best
            for (String name : names) {
                try {
                    ChannelHandler handler = future.channel().pipeline().get(name);

                    if (handler != null) {
                        if (childHandlerField == null) {
                            childHandlerField = handler.getClass().getDeclaredField("childHandler");
                            childHandlerField.setAccessible(true);
                        }

                        Object oldInit = childHandlerField.get(handler);

                        if (oldInit instanceof PEChannelInitializerModern) {
                            bootstrapAcceptor = handler;
                        }
                    }
                } catch (IllegalAccessException | NoSuchFieldException ignored) {
                }
            }

            // Default to first
            if (bootstrapAcceptor == null) {
                bootstrapAcceptor = future.channel().pipeline().first();
            }

            try {
                if (childHandlerField != null && bootstrapAcceptor != null) {
                    Object oldInit = childHandlerField.get(bootstrapAcceptor);

                    if (oldInit instanceof PEChannelInitializerModern) {
                        childHandlerField.set(bootstrapAcceptor, ((PEChannelInitializerModern) oldInit).getOldChannelInitializer());
                    }
                }
            } catch (IllegalAccessException ex) {
                PacketEvents.getPlugin().getLogger().severe("PacketEvents failed to eject the injection handler! Please reboot!");
            }
        }

        injectedFutures.clear();

        for (Map<Field, Object> map : injectedLists) {
            try {
                for (Map.Entry<Field, Object> entry : map.entrySet()) {
                    Field key = entry.getKey();
                    key.setAccessible(true);
                    Object object = entry.getValue();

                    if (object instanceof ListWrapper) {
                        key.set(object, ((ListWrapper) object).getOriginalList());
                    }
                }
            } catch (IllegalAccessException ex) {
                PacketEvents.getPlugin().getLogger().severe("PacketEvents failed to eject the"
                        + " injection handler! Please reboot!");
            }
        }


        injectedLists.clear();
    }

    @Override
    public void injectPlayer(Player player) {
        Object rawChannel = PacketEvents.getInstance().getPlayerUtils().getChannel(player);

        if (rawChannel != null) {
            updatePlayerObject(player, rawChannel);
        }
    }

    @Override
    public void ejectPlayer(Player player) {
        Object rawChannel = PacketEvents.getInstance().getPlayerUtils().getChannel(player);

        if (rawChannel != null) {
            Channel channel = (Channel) rawChannel;

            try {
                channel.pipeline().remove(PacketEvents.getInstance().getHandlerName() + "-decoder");
                channel.pipeline().remove(PacketEvents.getInstance().getHandlerName());
            } catch (RuntimeException ignored) {
            }
        }
    }

    @Override
    public boolean hasInjected(Player player) {
        Object rawChannel = PacketEvents.getInstance().getPlayerUtils().getChannel(player);

        if (rawChannel == null) {
            return false;
        }

        PlayerChannelHandlerModern handler = getHandler(rawChannel);
        PlayerDecodeHandlerModern decodeHandler = getDecoderHandler(rawChannel);
        return handler != null && decodeHandler != null;
    }

    @Override
    public void writePacket(Object rawChannel, Object rawNMSPacket) {
        Channel channel = (Channel) rawChannel;
        channel.write(rawNMSPacket);
    }

    @Override
    public void flushPackets(Object rawChannel) {
        Channel channel = (Channel) rawChannel;
        channel.flush();
    }

    @Override
    public void sendPacket(Object rawChannel, Object rawNMSPacket) {
        Channel channel = (Channel) rawChannel;
        channel.writeAndFlush(rawNMSPacket);
    }

    private static @Nullable PlayerDecodeHandlerModern getDecoderHandler(Object rawChannel) {
        Channel channel = (Channel) rawChannel;
        ChannelHandler handler = channel.pipeline().get(PacketEvents.getInstance().getHandlerName() + "-decoder");

        if (handler instanceof PlayerDecodeHandlerModern) {
            return (PlayerDecodeHandlerModern) handler;
        } else {
            return null;
        }
    }

    private static @Nullable PlayerChannelHandlerModern getHandler(Object rawChannel) {
        Channel channel = (Channel) rawChannel;
        ChannelHandler handler = channel.pipeline().get(PacketEvents.getInstance().getHandlerName());

        if (handler instanceof PlayerChannelHandlerModern) {
            return (PlayerChannelHandlerModern) handler;
        } else {
            return null;
        }
    }

    @Override
    public void updatePlayerObject(Player player, Object rawChannel) {
        Channel channel = (Channel) rawChannel;
        ChannelHandler decodeHandler = channel.pipeline().get(PacketEvents.getInstance().getHandlerName() + "-decoder");
        ChannelHandler channelHandler = channel.pipeline().get(PacketEvents.getInstance().getHandlerName());

        // Update the player object in the player decode handler.
        if (decodeHandler instanceof PlayerDecodeHandlerModern) {
            ((PlayerDecodeHandlerModern) decodeHandler).player.set(player);
        }

        // Update the player object in the player channel handler.
        if (channelHandler instanceof PlayerChannelHandlerModern) {
            ((PlayerChannelHandlerModern) channelHandler).player.set(player);
        }
    }
}
