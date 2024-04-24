package io.github.retrooper.packetevents.packetwrappers.play.in.entityaction;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public final class WrappedPacketInEntityAction extends WrappedPacketEntityAbstraction {

    private static Class<? extends Enum<?>> enumPlayerActionClass;

    public WrappedPacketInEntityAction(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        enumPlayerActionClass = NMSUtils.getNMSEnumClassWithoutException("EnumPlayerAction");

        if (enumPlayerActionClass == null) {
            enumPlayerActionClass = SubclassUtil.getEnumSubClass(PacketTypeClasses.Play.Client.ENTITY_ACTION, "EnumPlayerAction");
        }
    }

    public PlayerAction getAction() {
        if (enumPlayerActionClass == null) {
            int animationIndex = readInt(1) - 1;
            return PlayerAction.getByActionValue((byte) (animationIndex));
        } else {
            Enum<?> enumConst = readEnumConstant(0, enumPlayerActionClass);
            return PlayerAction.getByName(enumConst.name());
        }
    }

    public void setAction(PlayerAction action) throws UnsupportedOperationException {
        if (enumPlayerActionClass == null) {
            byte animationIndex = action.actionValue;
            writeInt(1, animationIndex + 1);
        } else {
            Enum<?> enumConst = EnumUtil.valueOf(enumPlayerActionClass, action.name());

            if (enumConst == null) {
                enumConst = EnumUtil.valueOf(enumPlayerActionClass, action.alias);
            }

            writeEnumConstant(0, enumConst);
        }
    }

    public int getJumpBoost() {
        if (enumPlayerActionClass == null) {
            return readInt(2);
        } else {
            return readInt(1);
        }
    }

    public void setJumpBoost(int jumpBoost) {
        if (enumPlayerActionClass == null) {
            writeInt(2, jumpBoost);
        } else {
            writeInt(1, jumpBoost);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum PlayerAction {
        START_SNEAKING((byte) 0, "PRESS_SHIFT_KEY"),
        STOP_SNEAKING((byte) 1, "RELEASE_SHIFT_KEY"),
        STOP_SLEEPING((byte) 2),
        START_SPRINTING((byte) 3),
        STOP_SPRINTING((byte) 4),
        RIDING_JUMP((byte) 5),
        START_RIDING_JUMP((byte) 5),
        STOP_RIDING_JUMP((byte) 6),
        OPEN_INVENTORY((byte) 7),
        START_FALL_FLYING((byte) 8);

        final byte actionValue;
        final String alias;

        PlayerAction(byte actionValue) {
            this.actionValue = actionValue;
            alias = "empty";
        }

        public static @Nullable PlayerAction getByActionValue(byte value) {
            if (value == RIDING_JUMP.actionValue) {
                return RIDING_JUMP;
            } else {
                for (PlayerAction action : values()) {
                    if (action.actionValue == value) {
                        return action;
                    }
                }
            }
            return null;
        }

        public static @Nullable PlayerAction getByName(String name) {
            for (PlayerAction action : values()) {
                if (action.name().equals(name) || action.alias.equals(name)) {
                    return action;
                }
            }
            return null;
        }
    }
}
