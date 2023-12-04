package net.foulest.vulture.check.type.badpackets;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockdig.WrappedPacketInBlockDig;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "BadPackets (G)", type = CheckType.BADPACKETS,
        description = "Detects breaking blocks too quickly.")
public class BadPacketsG extends Check {

    private int ticks;
    private int stage;

    public BadPacketsG(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            if (stage == 1) {
                ++ticks;
                stage = 2;
            } else {
                stage = 0;
            }

        } else if (packetId == PacketType.Play.Client.BLOCK_DIG) {
            WrappedPacketInBlockDig blockDig = new WrappedPacketInBlockDig(nmsPacket);
            WrappedPacketInBlockDig.PlayerDigType digType = blockDig.getDigType();

            if (digType == WrappedPacketInBlockDig.PlayerDigType.STOP_DESTROY_BLOCK) {
                stage = 1;

            } else if (digType == WrappedPacketInBlockDig.PlayerDigType.START_DESTROY_BLOCK) {
                if (stage == 2 && (ticks != 1 || playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8))) {
                    flag(false, "Break speed" + " (ticks=" + ticks + ")");
                }

                stage = 0;
                ticks = 0;
            }
        }
    }
}
