package io.github.retrooper.packetevents.packetwrappers.play.out.chat;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public final class WrappedPacketOutChat extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> chatClassConstructor;
    private static Class<? extends Enum<?>> chatMessageTypeEnum;

    // 0 = IChatBaseComponent, Byte
    // 1 = IChatBaseComponent, Int
    // 2 = IChatBaseComponent, ChatMessageType
    // 3 = IChatBaseComponent, ChatMessageType, UUID
    private static byte constructorMode;

    private String message;
    private ChatPosition chatPosition;
    private UUID uuid;

    public WrappedPacketOutChat(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutChat(String message, UUID uuid, boolean isJson) {
        this(message, ChatPosition.CHAT, uuid, isJson);
    }

    public WrappedPacketOutChat(BaseComponent component, UUID uuid) {
        this(component, ChatPosition.CHAT, uuid);
    }

    private WrappedPacketOutChat(BaseComponent component, ChatPosition pos, UUID uuid) {
        this(ComponentSerializer.toString(component), pos, uuid, true);
    }

    private WrappedPacketOutChat(String message, ChatPosition chatPosition, UUID uuid, boolean isJson) {
        this.uuid = uuid;
        this.message = isJson ? message : NMSUtils.fromStringToJSON(message);
        this.chatPosition = chatPosition;
    }

    @Override
    protected void load() {
        Class<?> packetClass = PacketTypeClasses.Play.Server.CHAT;
        chatMessageTypeEnum = NMSUtils.getNMSEnumClassWithoutException("ChatMessageType");

        if (chatMessageTypeEnum == null) {
            chatMessageTypeEnum = NMSUtils.getNMEnumClassWithoutException("network.chat.ChatMessageType");
        }

        if (chatMessageTypeEnum != null) {
            try {
                chatClassConstructor = packetClass.getConstructor(NMSUtils.iChatBaseComponentClass, chatMessageTypeEnum);
                constructorMode = 2;
            } catch (NoSuchMethodException e) {
                // Just a much newer version(1.16.x and above right now)
                try {
                    chatClassConstructor = packetClass.getConstructor(NMSUtils.iChatBaseComponentClass, chatMessageTypeEnum, UUID.class);
                    constructorMode = 3;
                } catch (NoSuchMethodException e2) {
                    // Failed to resolve the constructor
                    e2.printStackTrace();
                }
            }

        } else {
            try {
                chatClassConstructor = packetClass.getConstructor(NMSUtils.iChatBaseComponentClass, byte.class);
                constructorMode = 0;
            } catch (NoSuchMethodException e) {
                // That is fine, they are most likely on an older version.
                try {
                    chatClassConstructor = packetClass.getConstructor(NMSUtils.iChatBaseComponentClass, int.class);
                    constructorMode = 1;
                } catch (NoSuchMethodException e2) {
                    try {
                        // Some weird 1.7.10 spigots remove that int parameter for no reason, I won't keep adding
                        // support for any more spigots and might stop accepting pull requests for support for spigots
                        // breaking things that normal spigot has.
                        chatClassConstructor = packetClass.getConstructor(NMSUtils.iChatBaseComponentClass);
                        constructorMode = -1;
                    } catch (NoSuchMethodException e3) {
                        e3.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public @Nullable Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        byte chatPos = (byte) getChatPosition().ordinal();
        Enum<?> chatMessageTypeInstance = null;

        if (chatMessageTypeEnum != null) {
            chatMessageTypeInstance = EnumUtil.valueByIndex(chatMessageTypeEnum.asSubclass(Enum.class), chatPos);
        }

        switch (constructorMode) {
            case -1:
                return chatClassConstructor.newInstance(NMSUtils.generateIChatBaseComponent(getMessage()));
            case 0:
                return chatClassConstructor.newInstance(NMSUtils.generateIChatBaseComponent(getMessage()), chatPos);
            case 1:
                return chatClassConstructor.newInstance(NMSUtils.generateIChatBaseComponent(getMessage()), (int) chatPos);
            case 2:
                return chatClassConstructor.newInstance(NMSUtils.generateIChatBaseComponent(getMessage()), chatMessageTypeInstance);
            case 3:
                return chatClassConstructor.newInstance(NMSUtils.generateIChatBaseComponent(getMessage()), chatMessageTypeInstance, uuid);
            default:
                return null;
        }
    }

    /**
     * Get the message.
     *
     * @return Get String Message
     */
    private String getMessage() {
        if (nmsPacket != null) {
            return readIChatBaseComponent(0);
        } else {
            return message;
        }
    }

    public void setMessage(String message) {
        if (nmsPacket != null) {
            writeIChatBaseComponent(message);
        } else {
            this.message = message;
        }
    }

    /**
     * Get the chat position.
     * <p>
     * On 1.7.10, Only CHAT and SYSTEM_MESSAGE exist.
     * If an invalid chat position is sent, it will be defaulted it to CHAT.
     *
     * @return ChatPosition
     */
    private ChatPosition getChatPosition() {
        if (nmsPacket != null) {
            byte chatPositionValue;

            switch (constructorMode) {
                case -1:
                    chatPositionValue = (byte) ChatPosition.CHAT.ordinal();
                    break;

                case 0:
                    chatPositionValue = readByte(0);
                    break;

                case 1:
                    chatPositionValue = (byte) readInt(0);
                    break;

                case 2:
                case 3:
                    Enum<?> chatTypeEnumInstance = readEnumConstant(0, chatMessageTypeEnum);
                    return ChatPosition.values()[chatTypeEnumInstance.ordinal()];

                default:
                    chatPositionValue = 0;
                    break;
            }
            return ChatPosition.values()[chatPositionValue];
        } else {
            return chatPosition;
        }
    }

    public void setChatPosition(ChatPosition chatPosition) {
        if (nmsPacket != null) {
            switch (constructorMode) {
                case 0:
                    writeByte(0, (byte) chatPosition.ordinal());
                    break;

                case 1:
                    writeInt(0, chatPosition.ordinal());
                    break;

                case 2:
                case 3:
                    Enum<?> chatTypeEnumInstance = EnumUtil.valueByIndex(chatMessageTypeEnum.asSubclass(Enum.class), chatPosition.ordinal());
                    writeEnumConstant(0, chatTypeEnumInstance);
                    break;

                default:
                    break;
            }
        } else {
            this.chatPosition = chatPosition;
        }
    }

    public enum ChatPosition {
        CHAT,
        SYSTEM_MESSAGE,
        GAME_INFO
    }
}
