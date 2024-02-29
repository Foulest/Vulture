package dev._2lstudios.hamsterapi;

import dev._2lstudios.hamsterapi.handlers.HamsterChannelHandler;
import dev._2lstudios.hamsterapi.handlers.HamsterDecoderHandler;
import dev._2lstudios.hamsterapi.utils.Reflection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Getter;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.nio.channels.ClosedChannelException;

@Getter
public class HamsterAPI {

    public static Reflection reflection;
    public static final String DECODER_CHANNEL = "vulture_decoder";
    public static final String MAIN_CHANNEL = "vulture_channel";

    /**
     * Initializes HamsterAPI.
     */
    public static void initialize() {
        reflection = new Reflection(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
    }

    /**
     * Forcibly closes the player's connection.
     *
     * @param playerData The player's data.
     */
    public static void closeChannel(@NotNull PlayerData playerData) {
        Channel channel = playerData.getChannel();

        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

    /**
     * Removes handlers from the player's pipeline.
     *
     * @param playerData The player's data.
     */
    public static void uninject(@NotNull PlayerData playerData) {
        Channel channel = playerData.getChannel();
        boolean injected = playerData.isInjected();

        if (injected && channel != null && channel.isActive()) {
            ChannelPipeline pipeline = channel.pipeline();

            if (pipeline.get(DECODER_CHANNEL) != null) {
                pipeline.remove(DECODER_CHANNEL);
            }

            if (pipeline.get(MAIN_CHANNEL) != null) {
                pipeline.remove(MAIN_CHANNEL);
            }
        }
    }

    /**
     * Sets variables to simplify packet handling and injections.
     *
     * @param playerData The player's data.
     */
    public static void setup(@NotNull PlayerData playerData) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Player player = playerData.getPlayer();

        if (!playerData.isSetup()) {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            playerData.setPlayerConnection(reflection.getField(handle, reflection.getPlayerConnection()));
            playerData.setNetworkManager(reflection.getField(playerData.getPlayerConnection(), reflection.getNetworkManager()));
            playerData.setChannel((Channel) reflection.getField(playerData.getNetworkManager(), Channel.class));
            playerData.setIChatBaseComponentClass(reflection.getIChatBaseComponent());
            playerData.setSendPacketMethod(playerData.getPlayerConnection().getClass().getMethod("sendPacket", reflection.getPacket()));
            playerData.setToChatBaseComponent(playerData.getIChatBaseComponentClass().getDeclaredClasses()[0].getMethod("a", String.class));
            playerData.setSetup(true);
        }
    }

    /**
     * Injects handlers into the player's pipeline using NMS.
     *
     * @param playerData The player's data.
     */
    public static void inject(@NotNull PlayerData playerData) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, NoSuchFieldException, ClosedChannelException {
        if (!playerData.isInjected()) {
            setup(playerData);

            Channel channel = playerData.getChannel();

            if (!channel.isActive()) {
                throw new ClosedChannelException();
            }

            ChannelPipeline pipeline = channel.pipeline();
            ByteToMessageDecoder hamsterDecoderHandler = new HamsterDecoderHandler(playerData);
            ChannelDuplexHandler hamsterChannelHandler = new HamsterChannelHandler(playerData);

            if (pipeline.get("decompress") != null) {
                // Check if vulture_decoder is already in the pipeline
                if (pipeline.get(DECODER_CHANNEL) != null) {
                    pipeline.remove(DECODER_CHANNEL);
                }

                pipeline.addAfter("decompress", DECODER_CHANNEL, hamsterDecoderHandler);

            } else if (pipeline.get("splitter") != null) {
                // Check if vulture_decoder is already in the pipeline
                if (pipeline.get(DECODER_CHANNEL) != null) {
                    pipeline.remove(DECODER_CHANNEL);
                }

                pipeline.addAfter("splitter", DECODER_CHANNEL, hamsterDecoderHandler);

            } else {
                throw new IllegalAccessException("No ChannelHandler was found on the pipeline to inject " + DECODER_CHANNEL);
            }

            if (pipeline.get("decoder") != null) {
                pipeline.addAfter("decoder", MAIN_CHANNEL, hamsterChannelHandler);
            } else {
                throw new IllegalAccessException("No ChannelHandler was found on the pipeline to inject " + hamsterChannelHandler);
            }

            playerData.setInjected(true);
        }
    }

    /**
     * Injects, but instead of returning an exception, returns a boolean.
     *
     * @param playerData The player's data.
     * @return Whether or not the injection was successful.
     */
    public static boolean tryInject(PlayerData playerData) {
        try {
            setup(playerData);
            inject(playerData);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException
                 | NoSuchFieldException | ClosedChannelException ex) {
            MessageUtil.printException(ex);
            return false;
        }
        return true;
    }
}
