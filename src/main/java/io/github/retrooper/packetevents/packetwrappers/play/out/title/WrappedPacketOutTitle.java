package io.github.retrooper.packetevents.packetwrappers.play.out.title;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import lombok.AllArgsConstructor;

import java.lang.reflect.Constructor;

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

    public TitleAction getAction() {
        if (packet != null) {
            Enum<?> enumConst = readEnumConstant(0, enumTitleActionClass);
            return TitleAction.values()[enumConst.ordinal()];
        } else {
            return action;
        }
    }

    public void setAction(TitleAction action) {
        if (packet != null) {
            Enum<?> enumConst = EnumUtil.valueByIndex(enumTitleActionClass.asSubclass(Enum.class), action.ordinal());
            writeEnumConstant(0, enumConst);
        } else {
            this.action = action;
        }
    }

    public String getText() {
        if (packet != null) {
            return readIChatBaseComponent(0);
        } else {
            return text;
        }
    }

    public void setText(String text) {
        if (packet != null) {
            writeIChatBaseComponent(0, text);
        } else {
            this.text = text;
        }
    }

    public int getFadeInTicks() {
        if (packet != null) {
            return readInt(0);
        } else {
            return fadeInTicks;
        }
    }

    public void setFadeInTicks(int fadeInTicks) {
        if (packet != null) {
            writeInt(0, fadeInTicks);
        }
        this.fadeInTicks = fadeInTicks;
    }

    public int getStayTicks() {
        if (packet != null) {
            return readInt(1);
        } else {
            return stayTicks;
        }
    }

    public void setStayTicks(int stayTicks) {
        if (packet != null) {
            writeInt(1, stayTicks);
        } else {
            this.stayTicks = stayTicks;
        }
    }

    public int getFadeOutTicks() {
        if (packet != null) {
            return readInt(2);
        } else {
            return fadeOutTicks;
        }
    }

    public void setFadeOutTicks(int fadeOutTicks) {
        if (packet != null) {
            writeInt(2, fadeOutTicks);
        } else {
            this.fadeOutTicks = fadeOutTicks;
        }
    }

    @Override
    public Object asNMSPacket() throws Exception {
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
