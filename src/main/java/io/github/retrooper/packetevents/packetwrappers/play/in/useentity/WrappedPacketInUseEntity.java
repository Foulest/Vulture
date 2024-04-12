package io.github.retrooper.packetevents.packetwrappers.play.in.useentity;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.player.Hand;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public final class WrappedPacketInUseEntity extends WrappedPacketEntityAbstraction {

    private static Class<? extends Enum<?>> enumEntityUseActionClass;
    private static Class<?> obfuscatedDataInterface;
    private static Class<?> obfuscatedHandContainerClass;
    private static Class<?> obfuscatedTargetAndHandContainerClass;
    private static Method getObfuscatedEntityUseActionMethod;
    private static boolean v_1_9;
    private static boolean v_1_17;
    private EntityUseAction action;
    private Object obfuscatedDataObj;

    public WrappedPacketInUseEntity(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        v_1_9 = version.isNewerThanOrEquals(ServerVersion.v_1_9);
        v_1_17 = version.isNewerThanOrEquals(ServerVersion.v_1_17);

        try {
            enumEntityUseActionClass = NMSUtils.getNMSEnumClass("EnumEntityUseAction");
        } catch (ClassNotFoundException e) {
            // That is fine, it is probably a subclass
            if (v_1_17) {
                enumEntityUseActionClass = SubclassUtil.getEnumSubClass(PacketTypeClasses.Play.Client.USE_ENTITY, "b");
                obfuscatedDataInterface = SubclassUtil.getSubClass(PacketTypeClasses.Play.Client.USE_ENTITY, "EnumEntityUseAction");
                obfuscatedHandContainerClass = SubclassUtil.getSubClass(PacketTypeClasses.Play.Client.USE_ENTITY, "d");
                obfuscatedTargetAndHandContainerClass = SubclassUtil.getSubClass(PacketTypeClasses.Play.Client.USE_ENTITY, "e");
                getObfuscatedEntityUseActionMethod = Reflection.getMethod(obfuscatedDataInterface, enumEntityUseActionClass, 0);
            } else {
                enumEntityUseActionClass = SubclassUtil.getEnumSubClass(PacketTypeClasses.Play.Client.USE_ENTITY, "EnumEntityUseAction");
            }
        }
    }

    public Optional<Vector3d> getTarget() {
        if (getAction() != EntityUseAction.INTERACT_AT) {
            return Optional.empty();
        }

        Object vec3DObj;

        if (v_1_17) {
            if (obfuscatedDataObj == null) {
                obfuscatedDataObj = readObject(0, obfuscatedDataInterface);
            }

            if (obfuscatedTargetAndHandContainerClass.isInstance(obfuscatedDataObj)) {
                Object obfuscatedTargetAndHandContainerObj = obfuscatedTargetAndHandContainerClass.cast(obfuscatedDataObj);
                WrappedPacket wrappedTargetAndHandContainer = new WrappedPacket(new NMSPacket(obfuscatedTargetAndHandContainerObj));
                vec3DObj = wrappedTargetAndHandContainer.readObject(0, NMSUtils.vec3DClass);
            } else {
                return Optional.empty();
            }
        } else {
            vec3DObj = readObject(0, NMSUtils.vec3DClass);
        }

        WrappedPacket vec3DWrapper = new WrappedPacket(new NMSPacket(vec3DObj));
        return Optional.of(new Vector3d(vec3DWrapper.readDouble(0), vec3DWrapper.readDouble(1), vec3DWrapper.readDouble(2)));
    }

    public void setTarget(Vector3d target) {
        if (v_1_17) {
            Object vec3DObj = NMSUtils.generateVec3D(target.x, target.y, target.z);

            if (obfuscatedDataObj == null) {
                obfuscatedDataObj = readObject(0, obfuscatedDataInterface);
            }

            if (obfuscatedTargetAndHandContainerClass.isInstance(obfuscatedDataObj)) {
                Object obfuscatedTargetAndHandContainerObj = obfuscatedTargetAndHandContainerClass.cast(obfuscatedDataObj);
                WrappedPacket wrappedTargetAndHandContainer = new WrappedPacket(new NMSPacket(obfuscatedTargetAndHandContainerObj));
                wrappedTargetAndHandContainer.write(NMSUtils.vec3DClass, 0, vec3DObj);
            }
        }
    }

    public @Nullable EntityUseAction getAction() {
        if (action == null) {
            Enum<?> useActionEnum;

            if (v_1_17) {
                if (obfuscatedDataObj == null) {
                    obfuscatedDataObj = readObject(0, obfuscatedDataInterface);
                }

                try {
                    useActionEnum = (Enum<?>) getObfuscatedEntityUseActionMethod.invoke(obfuscatedDataObj);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                useActionEnum = readEnumConstant(0, enumEntityUseActionClass);

                if (useActionEnum == null) {
                    //This happens on some weird spigots apparently? Not sure why this is null.
                    return action = EntityUseAction.INTERACT;
                }
            }
            return action = EntityUseAction.values()[useActionEnum.ordinal()];
        }
        return action;
    }

    // TODO: Finish this
    private void setAction(@NotNull EntityUseAction action) {
        this.action = action;
        Enum<?> enumConst = EnumUtil.valueByIndex(enumEntityUseActionClass, action.ordinal());

        if (v_1_17) {
            // TODO: Add 1.17+ support
        } else {
            writeEnumConstant(0, enumConst);
        }
    }

    public Optional<Hand> getHand() {
        if (v_1_9 && (getAction() == EntityUseAction.INTERACT || getAction() == EntityUseAction.INTERACT_AT)) {
            Enum<?> enumHandConst;

            if (v_1_17) {
                if (obfuscatedDataObj == null) {
                    obfuscatedDataObj = readObject(0, obfuscatedDataInterface);
                }

                if (obfuscatedHandContainerClass.isInstance(obfuscatedDataObj)) {
                    Object obfuscatedHandContainerObj = obfuscatedHandContainerClass.cast(obfuscatedDataObj);
                    WrappedPacket wrappedHandContainer = new WrappedPacket(new NMSPacket(obfuscatedHandContainerObj));
                    enumHandConst = wrappedHandContainer.readEnumConstant(0, NMSUtils.enumHandClass);
                } else if (obfuscatedTargetAndHandContainerClass.isInstance(obfuscatedDataObj)) {
                    Object obfuscatedTargetAndHandContainerObj = obfuscatedTargetAndHandContainerClass.cast(obfuscatedDataObj);
                    WrappedPacket wrappedTargetAndHandContainer = new WrappedPacket(new NMSPacket(obfuscatedTargetAndHandContainerObj));
                    enumHandConst = wrappedTargetAndHandContainer.readEnumConstant(0, NMSUtils.enumHandClass);
                } else {
                    return Optional.empty();
                }
            } else {
                enumHandConst = readEnumConstant(0, NMSUtils.enumHandClass);
            }

            // Should actually never be null, but we will handle such a case
            if (enumHandConst == null) {
                return Optional.empty();
            }
            return Optional.of(Hand.values()[enumHandConst.ordinal()]);
        }
        return Optional.empty();
    }

    public void setHand(@NotNull Hand hand) {
        Enum<?> enumConst = EnumUtil.valueByIndex(NMSUtils.enumHandClass, hand.ordinal());

        if (v_1_17) {
            if (obfuscatedDataObj == null) {
                obfuscatedDataObj = readObject(0, obfuscatedDataInterface);
            }

            if (obfuscatedHandContainerClass.isInstance(obfuscatedDataObj)) {
                Object obfuscatedHandContainerObj = obfuscatedHandContainerClass.cast(obfuscatedDataObj);
                WrappedPacket wrappedHandContainer = new WrappedPacket(new NMSPacket(obfuscatedHandContainerObj));
                wrappedHandContainer.writeEnumConstant(0, enumConst);
            } else if (obfuscatedTargetAndHandContainerClass.isInstance(obfuscatedDataObj)) {
                Object obfuscatedTargetAndHandContainerObj = obfuscatedTargetAndHandContainerClass.cast(obfuscatedDataObj);
                WrappedPacket wrappedTargetAndHandContainer = new WrappedPacket(new NMSPacket(obfuscatedTargetAndHandContainerObj));
                wrappedTargetAndHandContainer.writeEnumConstant(0, enumConst);
            }
        } else if (v_1_9 && (getAction() == EntityUseAction.INTERACT || getAction() == EntityUseAction.INTERACT_AT)) {
            writeEnumConstant(0, enumConst);
        }
    }

    public enum EntityUseAction {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}
