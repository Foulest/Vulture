package net.foulest.packetevents.packetwrappers.play.in.useentity;

import net.foulest.packetevents.packettype.PacketTypeClasses;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.packetwrappers.api.WrapperPacketReader;
import net.foulest.packetevents.packetwrappers.api.helper.WrappedPacketEntityAbstraction;
import net.foulest.packetevents.utils.enums.EnumUtil;
import net.foulest.packetevents.utils.nms.NMSUtils;
import net.foulest.packetevents.utils.reflection.SubclassUtil;
import net.foulest.packetevents.utils.vector.Vector3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class WrappedPacketInUseEntity extends WrappedPacketEntityAbstraction {

    private static Class<? extends Enum<?>> enumEntityUseActionClass;
    private EntityUseAction action;

    public WrappedPacketInUseEntity(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        try {
            enumEntityUseActionClass = NMSUtils.getNMSEnumClass("EnumEntityUseAction");
        } catch (ClassNotFoundException ex) {
            // Probably a subclass
            enumEntityUseActionClass = SubclassUtil.getEnumSubClass(PacketTypeClasses.Play.Client.USE_ENTITY, "EnumEntityUseAction");
        }
    }

    public Optional<Vector3d> getTarget() {
        if (getAction() != EntityUseAction.INTERACT_AT) {
            return Optional.empty();
        }

        Object vec3DObj = readObject(0, NMSUtils.vec3DClass);
        WrapperPacketReader vec3DWrapper = new WrappedPacket(new NMSPacket(vec3DObj));
        return Optional.of(new Vector3d(vec3DWrapper.readDouble(0), vec3DWrapper.readDouble(1), vec3DWrapper.readDouble(2)));
    }

    public EntityUseAction getAction() {
        if (action == null) {
            Enum<?> useActionEnum = readEnumConstant(0, enumEntityUseActionClass);

            if (useActionEnum == null) {
                action = EntityUseAction.INTERACT;
                // This happens on some weird spigots apparently? Not sure why this is null.
                return action;
            }
            action = EntityUseAction.values()[useActionEnum.ordinal()];
            return action;
        }
        return action;
    }

    private void setAction(@NotNull EntityUseAction action) {
        this.action = action;
        Enum<?> enumConst = EnumUtil.valueByIndex(enumEntityUseActionClass.asSubclass(Enum.class), action.ordinal());
        writeEnumConstant(0, enumConst);
    }

    public enum EntityUseAction {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}
