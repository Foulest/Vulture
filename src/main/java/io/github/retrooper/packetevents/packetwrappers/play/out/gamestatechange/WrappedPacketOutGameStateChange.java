package io.github.retrooper.packetevents.packetwrappers.play.out.gamestatechange;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketReader;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketWriter;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
@AllArgsConstructor
public class WrappedPacketOutGameStateChange extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> packetConstructor;
    private static Constructor<?> reasonClassConstructor;
    private static Class<?> reasonClassType;
    private static boolean reasonIntMode;
    private static boolean valueFloatMode;

    private int reason;
    private double value;

    public WrappedPacketOutGameStateChange(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutGameStateChange(int reason, float value) {
        this(reason, (double) value);
    }

    @Override
    protected void load() {
        reasonClassType = SubclassUtil.getSubClass(PacketTypeClasses.Play.Server.GAME_STATE_CHANGE, 0);

        if (reasonClassType != null) {
            try {
                reasonClassConstructor = reasonClassType.getConstructor(int.class);
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }

        reasonIntMode = reasonClassType == null;

        if (reasonIntMode) {
            reasonClassType = int.class;
        }

        valueFloatMode = Reflection.getField(PacketTypeClasses.Play.Server.GAME_STATE_CHANGE, double.class, 0) == null;

        try {
            Class<?> valueClassType;

            if (valueFloatMode) {
                valueClassType = float.class;
            } else {
                // Just an older version(1.7.10/1.8.x or so)
                valueClassType = double.class;
            }

            packetConstructor = PacketTypeClasses.Play.Server.GAME_STATE_CHANGE.getConstructor(reasonClassType, valueClassType);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    private int getReason() {
        if (nmsPacket != null) {
            if (reasonIntMode) {
                return readInt(0);
            } else {
                // this packet is obfuscated quite strongly(1.16), so we must do this
                Object reasonObject = readObject(12, reasonClassType);
                WrapperPacketReader reasonObjWrapper = new WrappedPacket(new NMSPacket(reasonObject));
                return reasonObjWrapper.readInt(0);
            }
        } else {
            return reason;
        }
    }

    public void setReason(int reason) {
        if (nmsPacket != null) {
            if (reasonIntMode) {
                writeInt(0, reason);
            } else {
                // this packet is obfuscated quite strongly(1.16), so we must do this
                Object reasonObj = readObject(12, reasonClassType);
                WrapperPacketWriter reasonObjWrapper = new WrappedPacket(new NMSPacket(reasonObj));
                reasonObjWrapper.writeInt(0, reason);
            }
        } else {
            this.reason = reason;
        }
    }

    private double getValue() {
        if (nmsPacket != null) {
            if (valueFloatMode) {
                return readFloat(0);
            } else {
                return readDouble(0);
            }
        } else {
            return value;
        }
    }

    public void setValue(double value) {
        if (nmsPacket != null) {
            if (valueFloatMode) {
                writeFloat(0, (float) value);
            } else {
                writeDouble(0, value);
            }
        } else {
            this.value = value;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (reasonIntMode) {
            if (valueFloatMode) {
                return packetConstructor.newInstance(getReason(), (float) getValue());
            } else {
                return packetConstructor.newInstance(getReason(), getValue());
            }
        } else {
            Object reasonObject = reasonClassConstructor.newInstance(getReason());

            if (valueFloatMode) {
                return packetConstructor.newInstance(reasonObject, (float) getValue());
            } else {
                return packetConstructor.newInstance(reasonObject, getValue());
            }
        }
    }
}
