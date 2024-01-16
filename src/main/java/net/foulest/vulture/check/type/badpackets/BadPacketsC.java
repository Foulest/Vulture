package net.foulest.vulture.check.type.badpackets;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;

@CheckInfo(name = "BadPackets (C)", type = CheckType.BADPACKETS, punishable = false,
        description = "Detects sending attack packets without swinging.")
public class BadPacketsC extends Check {

    private boolean swung;

    public BadPacketsC(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        // Checks the player for exemptions.
        if (playerData.getVersion().isNewerThan(ClientVersion.v_1_8)) {
            return;
        }

        if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            WrappedPacketInUseEntity.EntityUseAction action = useEntity.getAction();

            if (action == WrappedPacketInUseEntity.EntityUseAction.ATTACK) {
                if (!swung) {
                    KickUtil.kickPlayer(player, event, "Attacked an entity without swinging");
                    return;
                }

                swung = false;
            }

        } else if (packetId == PacketType.Play.Client.ARM_ANIMATION) {
            swung = true;

        } else if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            swung = false;
        }
    }
}
