package net.foulest.vulture.check.type.autoblock;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockdig.WrappedPacketInBlockDig;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "AutoBlock (C)", type = CheckType.AUTOBLOCK,
        description = "Detects sending invalid BlockPlace and ReleaseUseItem order.")
public class AutoBlockC extends Check {

    private int buffer;

    public AutoBlockC(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.BLOCK_PLACE) {
            boolean blocking = playerData.isBlocking();
            boolean eating = playerData.isEating();
            boolean drinking = playerData.isDrinking();
            boolean shootingBow = playerData.isShootingBow();

            if (blocking || eating || drinking || shootingBow) {
                buffer = 0;
            }

        } else if (packetId == PacketType.Play.Client.BLOCK_DIG) {
            WrappedPacketInBlockDig blockDig = new WrappedPacketInBlockDig(nmsPacket);
            WrappedPacketInBlockDig.PlayerDigType digType = blockDig.getDigType();

            if (digType == WrappedPacketInBlockDig.PlayerDigType.RELEASE_USE_ITEM) {
                if (++buffer > 1) {
                    flag(false);
                }
            }
        }
    }
}
