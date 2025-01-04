package net.foulest.packetevents.packetwrappers.play.in.clientcommand;

import net.foulest.packetevents.packettype.PacketTypeClasses;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.utils.enums.EnumUtil;
import net.foulest.packetevents.utils.nms.NMSUtils;
import net.foulest.packetevents.utils.reflection.SubclassUtil;
import org.jetbrains.annotations.NotNull;

public final class WrappedPacketInClientCommand extends WrappedPacket {

    private static Class<? extends Enum<?>> enumClientCommandClass;

    public WrappedPacketInClientCommand(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        enumClientCommandClass = NMSUtils.getNMSEnumClassWithoutException("EnumClientCommand");

        if (enumClientCommandClass == null) {
            // Probably a subclass
            enumClientCommandClass = SubclassUtil.getEnumSubClass(PacketTypeClasses.Play.Client.CLIENT_COMMAND, "EnumClientCommand");
        }
    }

    public ClientCommand getClientCommand() {
        Enum<?> enumConst = readEnumConstant(0, enumClientCommandClass);
        return ClientCommand.values()[enumConst.ordinal()];
    }

    public void setClientCommand(@NotNull ClientCommand command) {
        Enum<?> enumConst = EnumUtil.valueByIndex(enumClientCommandClass.asSubclass(Enum.class), command.ordinal());
        writeEnumConstant(0, enumConst);
    }

    public enum ClientCommand {
        PERFORM_RESPAWN,
        REQUEST_STATS,
        OPEN_INVENTORY_ACHIEVEMENT
    }
}
