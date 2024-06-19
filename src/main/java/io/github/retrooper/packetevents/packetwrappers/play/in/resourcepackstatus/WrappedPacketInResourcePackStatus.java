package io.github.retrooper.packetevents.packetwrappers.play.in.resourcepackstatus;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import org.jetbrains.annotations.NotNull;

public class WrappedPacketInResourcePackStatus extends WrappedPacket {

    private static Class<? extends Enum<?>> enumResourcePackStatusClass;

    public WrappedPacketInResourcePackStatus(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        enumResourcePackStatusClass = SubclassUtil.getEnumSubClass(PacketTypeClasses.Play.Client.RESOURCE_PACK_STATUS, "EnumResourcePackStatus");
    }

    public ResourcePackStatus getStatus() {
        Enum<?> enumConst = readEnumConstant(0, enumResourcePackStatusClass);
        return ResourcePackStatus.values()[enumConst.ordinal()];
    }

    public void setStatus(@NotNull ResourcePackStatus status) {
        Enum<?> enumConst = EnumUtil.valueByIndex(enumResourcePackStatusClass.asSubclass(Enum.class), status.ordinal());
        writeEnumConstant(0, enumConst);
    }

    public enum ResourcePackStatus {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED
    }
}
