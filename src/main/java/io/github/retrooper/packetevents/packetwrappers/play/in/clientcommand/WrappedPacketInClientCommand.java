package io.github.retrooper.packetevents.packetwrappers.play.in.clientcommand;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
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

    public void setClientCommand(@NotNull ClientCommand command) throws UnsupportedOperationException {
        Enum<?> enumConst = EnumUtil.valueByIndex(enumClientCommandClass, command.ordinal());
        writeEnumConstant(0, enumConst);
    }

    public enum ClientCommand {
        PERFORM_RESPAWN,
        REQUEST_STATS,
        OPEN_INVENTORY_ACHIEVEMENT
    }
}
