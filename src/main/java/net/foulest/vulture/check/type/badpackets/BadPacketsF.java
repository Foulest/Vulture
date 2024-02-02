package net.foulest.vulture.check.type.badpackets;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "BadPackets (F)", type = CheckType.BADPACKETS, acceptsServerPackets = true,
        description = "Detects sending invalid UpdateSign packets.")
public class BadPacketsF extends Check {

    private boolean sentUpdateSign;
    private boolean sentSignEditor;
    private boolean sentBlockChange;

    public BadPacketsF(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Server.OPEN_SIGN_EDITOR) {
            sentSignEditor = true;
            sentBlockChange = false;

        } else if (packetId == PacketType.Play.Server.BLOCK_CHANGE) {
            sentBlockChange = true;
            sentSignEditor = false;

        } else if (packetId == PacketType.Play.Client.UPDATE_SIGN) {
            if (!sentSignEditor) {
                flag(false, event, "Sent UpdateSign packet without SignEditor");
            }

            sentUpdateSign = true;

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            if (sentUpdateSign && !sentBlockChange) {
                flag(false, "Sent UpdateSign packet without BlockChange");
            }

            sentUpdateSign = false;
            sentSignEditor = false;
            sentBlockChange = false;
        }
    }
}
