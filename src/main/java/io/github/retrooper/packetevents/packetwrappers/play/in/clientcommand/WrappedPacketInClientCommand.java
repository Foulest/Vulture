package io.github.retrooper.packetevents.packetwrappers.play.in.clientcommand;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import io.github.retrooper.packetevents.utils.server.ServerVersion;

public final class WrappedPacketInClientCommand extends WrappedPacket {

    private static boolean v_1_16;
    private static Class<? extends Enum<?>> enumClientCommandClass;

    public WrappedPacketInClientCommand(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        v_1_16 = version.isNewerThanOrEquals(ServerVersion.v_1_16);
        enumClientCommandClass = NMSUtils.getNMSEnumClassWithoutException("EnumClientCommand");

        if (enumClientCommandClass == null) {
            //Probably a subclass
            enumClientCommandClass = SubclassUtil.getEnumSubClass(PacketTypeClasses.Play.Client.CLIENT_COMMAND, "EnumClientCommand");
        }
    }

    public ClientCommand getClientCommand() {
        Enum<?> enumConst = readEnumConstant(0, enumClientCommandClass);
        return ClientCommand.values()[enumConst.ordinal()];
    }

    public void setClientCommand(ClientCommand command) throws UnsupportedOperationException {
        if (command == ClientCommand.OPEN_INVENTORY_ACHIEVEMENT && v_1_16) {
            throwUnsupportedOperation(command);
        }

        Enum<?> enumConst = EnumUtil.valueByIndex(enumClientCommandClass, command.ordinal());
        writeEnumConstant(0, enumConst);
    }

    public enum ClientCommand {
        PERFORM_RESPAWN,
        REQUEST_STATS,

        @SupportedVersions(ranges = {ServerVersion.v_1_7_10, ServerVersion.v_1_15_2})
        OPEN_INVENTORY_ACHIEVEMENT
    }
}
