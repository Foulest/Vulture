package net.foulest.vulture.check.type.autoclicker;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

import java.util.LinkedList;
import java.util.Queue;

@CheckInfo(name = "AutoClicker (A)", type = CheckType.AUTOCLICKER)
public class AutoClickerA extends Check {

    private double buffer;
    public boolean swung;
    public final Queue<Integer> flyingCountQueue = new LinkedList<>();
    public int flyingCount;

    public AutoClickerA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            boolean placingBlock = playerData.isPlacingBlock();
            boolean digging = playerData.isDigging();

            if (swung && !placingBlock && !digging && flyingCount < 8) {
                flyingCountQueue.add(flyingCount);

                if (flyingCountQueue.size() == 100) {
                    double average = flyingCountQueue.stream().mapToDouble(d -> d).average().orElse(0.0);
                    double stdDev = 0.0;

                    for (Integer i : flyingCountQueue) {
                        stdDev += Math.pow(i.doubleValue() - average, 2.0);
                    }

                    stdDev /= flyingCountQueue.size();

                    if (stdDev <= 0.28) {
                        if (++buffer > 5) {
                            flag(false, "stdDev=" + stdDev
                                    + "buffer=" + buffer);
                        }
                    } else {
                        buffer = Math.max(buffer - 1.5, 0.0);
                    }

                    flyingCountQueue.clear();
                }

                flyingCount = 0;
            }

            swung = false;
            ++flyingCount;

        } else if (packetId == PacketType.Play.Client.ARM_ANIMATION) {
            swung = true;
        }
    }
}
