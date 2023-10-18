package net.foulest.vulture.check.type.autoclicker;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.blockdig.WrappedPacketInBlockDig;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "AutoClicker (C)", type = CheckType.AUTOCLICKER)
public class AutoClickerC extends Check {

    private int clicks, outliers, flyingCount;
    private boolean release;
    private double buffer;

    public AutoClickerC(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (playerData.isDigging()) {
            return;
        }

        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            ++flyingCount;

        } else if (packetId == PacketType.Play.Client.BLOCK_DIG) {
            WrappedPacketInBlockDig blockDig = new WrappedPacketInBlockDig(nmsPacket);
            WrappedPacketInBlockDig.PlayerDigType digType = blockDig.getDigType();

            if (digType == WrappedPacketInBlockDig.PlayerDigType.RELEASE_USE_ITEM) {
                release = true;
            }

        } else if (packetId == PacketType.Play.Client.ARM_ANIMATION) {
            boolean placingBlock = playerData.isPlacingBlock();

            if (flyingCount < 10 && flyingCount != 0) {
                if (release) {
                    release = false;
                    flyingCount = 0;
                    return;
                }

                if (flyingCount > 3 && !placingBlock) {
                    ++outliers;
                }

                if (++clicks == 80) {
                    if (outliers == 0) {
                        if ((buffer += 1.4) >= 6.8) {
                            flag("outliers=" + outliers
                                    + " buffer=" + buffer);
                        }
                    } else {
                        buffer = Math.max(buffer - 0.8, 0);
                    }

                    outliers = 0;
                    clicks = 0;
                }
            }

            flyingCount = 0;
        }
    }
}
