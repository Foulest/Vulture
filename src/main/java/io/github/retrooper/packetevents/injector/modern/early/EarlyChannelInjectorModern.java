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
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class EarlyChannelInjectorModern implements EarlyInjector {

    private final List<ChannelFuture> injectedFutures = new ArrayList<>();
    private final List<Map<Field, Object>> injectedLists = new ArrayList<>();

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
                        for (Object object : (List) value) {
                            if (object instanceof ChannelFuture) {
                                return true;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
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
                Object value = null;

                try {
                    value = field.get(serverConnection);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }

                if (value instanceof List) {
                    List listWrapper = new ListWrapper((List) value) {
                        @Override
                        public void processAdd(Object object) {
                            if (object instanceof ChannelFuture) {
                                try {
                                    injectChannelFuture((ChannelFuture) object);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    };

                    HashMap<Field, Object> map = new HashMap<>();
                    map.put(field, serverConnection);
                    injectedLists.add(map);

                    field.set(serverConnection, listWrapper);

                    synchronized (listWrapper) {
                        for (Object serverChannel : (List) value) {
                            if (serverChannel instanceof ChannelFuture) {
                                injectChannelFuture((ChannelFuture) serverChannel);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
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
                if (pipeline.get(PacketEvents.get().getHandlerName()) != null) {
                    pipeline.remove(PacketEvents.get().getHandlerName());
                }
                if (pipeline.get(PacketEvents.get().getHandlerName() + "-decoder") != null) {
                    pipeline.remove(PacketEvents.get().getHandlerName() + "-decoder");
                }

                ByteToMessageDecoder decodeHandler = new PlayerDecodeHandlerModern();
                ChannelDuplexHandler channelHandler = new PlayerChannelHandlerModern();

                // Add the new handlers.
                if (pipeline.get("splitter") != null) {
                    pipeline.addAfter("splitter", PacketEvents.get().getHandlerName() + "-decoder", decodeHandler);
                }
                if (pipeline.get("packet_handler") != null) {
                    pipeline.addBefore("packet_handler", PacketEvents.get().getHandlerName(), channelHandler);
                }
            }
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
                bootstrapAcceptorField.get(handler);
                bootstrapAcceptor = handler;
            } catch (Exception ignored) {
            }
        }

        if (bootstrapAcceptor == null) {
            bootstrapAcceptor = channelFuture.channel().pipeline().first();
        }

        ChannelInitializer<?> oldChannelInitializer;

        try {
            Validate.notNull(bootstrapAcceptorField, "Failed to find the 'childHandler' field in the channel pipeline!");
            oldChannelInitializer = (ChannelInitializer<?>) bootstrapAcceptorField.get(bootstrapAcceptor);
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
        for (Player player : PacketEvents.get().getPlugin().getServer().getOnlinePlayers()) {
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
                } catch (Exception ignored) {
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
            } catch (Exception e) {
                PacketEvents.get().getPlugin().getLogger().severe("PacketEvents failed to eject the injection handler! Please reboot!");
            }
        }

        injectedFutures.clear();

        for (Map<Field, Object> map : injectedLists) {
            try {
                for (Field key : map.keySet()) {
                    key.setAccessible(true);
                    Object object = map.get(key);

                    if (object instanceof ListWrapper) {
                        key.set(object, ((ListWrapper) object).getOriginalList());
                    }
                }
            } catch (IllegalAccessException ex) {
                PacketEvents.get().getPlugin().getLogger().severe("PacketEvents failed to eject the"
                        + " injection handler! Please reboot!");
            }
        }

        injectedLists.clear();
    }

    @Override
    public void injectPlayer(Player player) {
        Object rawChannel = PacketEvents.get().getPlayerUtils().getChannel(player);

        if (rawChannel != null) {
            updatePlayerObject(player, rawChannel);
        }
    }

    @Override
    public void ejectPlayer(Player player) {
        Object rawChannel = PacketEvents.get().getPlayerUtils().getChannel(player);

        if (rawChannel != null) {
            Channel channel = (Channel) rawChannel;

            try {
                channel.pipeline().remove(PacketEvents.get().getHandlerName() + "-decoder");
                channel.pipeline().remove(PacketEvents.get().getHandlerName());
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean hasInjected(Player player) {
        Object rawChannel = PacketEvents.get().getPlayerUtils().getChannel(player);

        if (rawChannel == null) {
            return false;
        }

        PlayerChannelHandlerModern handler = getHandler(rawChannel);
        PlayerDecodeHandlerModern decodeHandler = getDecoderHandler(rawChannel);
        return handler != null && handler.player != null && decodeHandler != null && decodeHandler.player != null;
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

    private @Nullable PlayerDecodeHandlerModern getDecoderHandler(Object rawChannel) {
        Channel channel = (Channel) rawChannel;
        ChannelHandler handler = channel.pipeline().get(PacketEvents.get().getHandlerName() + "-decoder");

        if (handler instanceof PlayerDecodeHandlerModern) {
            return (PlayerDecodeHandlerModern) handler;
        } else {
            return null;
        }
    }

    private @Nullable PlayerChannelHandlerModern getHandler(Object rawChannel) {
        Channel channel = (Channel) rawChannel;
        ChannelHandler handler = channel.pipeline().get(PacketEvents.get().getHandlerName());

        if (handler instanceof PlayerChannelHandlerModern) {
            return (PlayerChannelHandlerModern) handler;
        } else {
            return null;
        }
    }

    @Override
    public void updatePlayerObject(Player player, Object rawChannel) {
        Channel channel = (Channel) rawChannel;
        ChannelHandler decodeHandler = channel.pipeline().get(PacketEvents.get().getHandlerName() + "-decoder");
        ChannelHandler channelHandler = channel.pipeline().get(PacketEvents.get().getHandlerName());

        // Update the player object in the player decode handler.
        if (decodeHandler instanceof PlayerDecodeHandlerModern) {
            ((PlayerDecodeHandlerModern) decodeHandler).player = player;
        }

        // Update the player object in the player channel handler.
        if (channelHandler instanceof PlayerChannelHandlerModern) {
            ((PlayerChannelHandlerModern) channelHandler).player = player;
        }
    }
}
