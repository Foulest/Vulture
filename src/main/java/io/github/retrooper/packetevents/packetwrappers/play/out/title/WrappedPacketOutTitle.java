package io.github.retrooper.packetevents.packetwrappers.play.out.title;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
@AllArgsConstructor
public class WrappedPacketOutTitle extends WrappedPacket implements SendableWrapper {

    private static Class<? extends Enum<?>> enumTitleActionClass;
    private static Constructor<?> packetConstructor;

    private TitleAction action;
    private String text;
    private int fadeInTicks;
    private int stayTicks;
    private int fadeOutTicks;

    public WrappedPacketOutTitle(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        enumTitleActionClass = SubclassUtil.getEnumSubClass(PacketTypeClasses.Play.Server.TITLE, 0);

        try {
            packetConstructor = PacketTypeClasses.Play.Server.TITLE.getConstructor(enumTitleActionClass,
                    NMSUtils.iChatBaseComponentClass, int.class, int.class, int.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private TitleAction getAction() {
        if (nmsPacket != null) {
            Enum<?> enumConst = readEnumConstant(0, enumTitleActionClass);
            return TitleAction.values()[enumConst.ordinal()];
        } else {
            return action;
        }
    }

    public void setAction(TitleAction action) {
        if (nmsPacket != null) {
            Enum<?> enumConst = EnumUtil.valueByIndex(enumTitleActionClass.asSubclass(Enum.class), action.ordinal());
            writeEnumConstant(0, enumConst);
        } else {
            this.action = action;
        }
    }

    private String getText() {
        if (nmsPacket != null) {
            return readIChatBaseComponent(0);
        } else {
            return text;
        }
    }

    public void setText(String text) {
        if (nmsPacket != null) {
            writeIChatBaseComponent(text);
        } else {
            this.text = text;
        }
    }

    private int getFadeInTicks() {
        if (nmsPacket != null) {
            return readInt(0);
        } else {
            return fadeInTicks;
        }
    }

    public void setFadeInTicks(int fadeInTicks) {
        if (nmsPacket != null) {
            writeInt(0, fadeInTicks);
        }
        this.fadeInTicks = fadeInTicks;
    }

    private int getStayTicks() {
        if (nmsPacket != null) {
            return readInt(1);
        } else {
            return stayTicks;
        }
    }

    public void setStayTicks(int stayTicks) {
        if (nmsPacket != null) {
            writeInt(1, stayTicks);
        } else {
            this.stayTicks = stayTicks;
        }
    }

    private int getFadeOutTicks() {
        if (nmsPacket != null) {
            return readInt(2);
        } else {
            return fadeOutTicks;
        }
    }

    public void setFadeOutTicks(int fadeOutTicks) {
        if (nmsPacket != null) {
            writeInt(2, fadeOutTicks);
        } else {
            this.fadeOutTicks = fadeOutTicks;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Enum<?> enumConst = EnumUtil.valueByIndex(enumTitleActionClass.asSubclass(Enum.class), getAction().ordinal());
        return packetConstructor.newInstance(enumConst, NMSUtils.generateIChatBaseComponent(getText()),
                getFadeInTicks(), getStayTicks(), getFadeOutTicks());
    }

    public enum TitleAction {
        TITLE,
        SUBTITLE,
        TIMES,
        CLEAR,
        RESET
    }
}
