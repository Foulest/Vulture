package io.github.retrooper.packetevents.packetwrappers.play.in.settings;

import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class WrappedPacketInSettings extends WrappedPacket {

    private static Class<? extends Enum<?>> chatVisibilityEnumClass;

    public WrappedPacketInSettings(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        chatVisibilityEnumClass = NMSUtils.getNMSEnumClassWithoutException("EnumChatVisibility");

        if (chatVisibilityEnumClass == null) {
            // They are on 1.17+
            chatVisibilityEnumClass = NMSUtils.getNMEnumClassWithoutException("world.entity.player.EnumChatVisibility");

            if (chatVisibilityEnumClass == null) {
                // They are just on an outdated version
                chatVisibilityEnumClass = SubclassUtil.getEnumSubClass(NMSUtils.entityHumanClass, "EnumChatVisibility");
            }
        }
    }

    public String getLocale() {
        return readString(0);
    }

    public void setLocale(String locale) {
        writeString(0, locale);
    }

    public int getViewDistance() {
        return readInt(0);
    }

    public void setViewDistance(int viewDistance) {
        writeInt(0, viewDistance);
    }

    public ChatVisibility getChatVisibility() {
        Enum<?> enumConst = readEnumConstant(0, chatVisibilityEnumClass);
        return ChatVisibility.values()[enumConst.ordinal()];
    }

    public void setChatVisibility(@NotNull ChatVisibility visibility) {
        Enum<?> enumConst = EnumUtil.valueByIndex(chatVisibilityEnumClass, visibility.ordinal());
        writeEnumConstant(0, enumConst);
    }

    public boolean isChatColored() {
        return readBoolean(0);
    }

    public void setChatColored(boolean chatColors) {
        writeBoolean(0, chatColors);
    }

    public byte getDisplaySkinPartsMask() {
        return (byte) readInt(1);
    }

    public void setDisplaySkinPartsMask(byte mask) {
        writeInt(1, mask);
    }

    public Set<DisplayedSkinPart> getDisplayedSkinParts() {
        Set<DisplayedSkinPart> displayedSkinParts = new HashSet<>();
        byte mask = getDisplaySkinPartsMask();

        for (DisplayedSkinPart part : DisplayedSkinPart.values()) {
            if ((mask & part.maskFlag) == part.maskFlag) {
                displayedSkinParts.add(part);
            }
        }
        return displayedSkinParts;
    }

    public void setDisplayedSkinParts(@NotNull Set<DisplayedSkinPart> displayedSkinParts) {
        byte mask = 0;

        for (DisplayedSkinPart part : displayedSkinParts) {
            mask |= part.maskFlag;
        }

        setDisplaySkinPartsMask(mask);
    }

    public enum ChatVisibility {
        FULL,
        SYSTEM,
        HIDDEN
    }

    public enum DisplayedSkinPart {
        CAPE(0x01),
        JACKET(0x02),
        LEFT_SLEEVE(0x04),
        RIGHT_SLEEVE(0x08),
        LEFT_PANTS(0x10),
        RIGHT_PANTS(0x20),
        HAT(0x40);

        final byte maskFlag;

        DisplayedSkinPart(int maskFlag) {
            this.maskFlag = (byte) maskFlag;
        }
    }
}
