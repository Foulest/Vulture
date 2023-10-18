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

import java.util.LinkedList;

@CheckInfo(name = "AutoClicker (B)", type = CheckType.AUTOCLICKER)
public class AutoClickerB extends Check {

    private double buffer;
    public boolean release;
    public final LinkedList<Integer> recentCounts = new LinkedList<>();
    public int flyingCount;

    public AutoClickerB(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
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
            boolean digging = playerData.isDigging();

            if (!placingBlock && !digging) {
                if (flyingCount < 8) {
                    if (release) {
                        release = false;
                        flyingCount = 0;
                        return;
                    }

                    recentCounts.add(flyingCount);

                    if (recentCounts.size() == 100) {
                        double average = 0.0;
                        for (int i : recentCounts) {
                            average += i;
                        }
                        average /= recentCounts.size();

                        double stdDev = 0.0;
                        for (int j : recentCounts) {
                            stdDev += Math.pow(j - average, 2.0);
                        }
                        stdDev /= recentCounts.size();
                        stdDev = Math.sqrt(stdDev);

                        if (stdDev < 0.4) {
                            if ((buffer += 1.4) >= 5.0) {
                                flag("stdDev=" + stdDev
                                        + " buffer=" + buffer);
                            }
                        } else {
                            buffer = Math.max(buffer - 0.8, 0.0);
                        }

                        recentCounts.clear();
                    }
                }

                flyingCount = 0;
            }
        }
    }
}
