package net.foulest.vulture.check.type.autoclicker;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MathUtil;
import net.foulest.vulture.util.data.EvictingList;

import java.util.LinkedList;
import java.util.Queue;

@CheckInfo(name = "AutoClicker (A)", type = CheckType.AUTOCLICKER,
        description = "Checks for unusually low standard deviation in click times.")
public class AutoClickerA extends Check {

    private boolean swung;
    private int flyingCount;
    private final Queue<Integer> flyingCountQueue = new LinkedList<>();
    private final EvictingList<Double> stdDeviations = new EvictingList<>(5);

    public AutoClickerA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            boolean placingBlock = playerData.isPlacingBlock();
            boolean digging = playerData.isDigging();

            if (swung && !placingBlock && !digging) {
                if (flyingCount <= 10) {
                    flyingCountQueue.add(flyingCount);

                    // Checks the last 10 clicks and clears the queue after.
                    if (flyingCountQueue.size() == 10) {
                        double stdDev = MathUtil.getStandardDeviation(flyingCountQueue);
                        stdDeviations.add(stdDev);

                        // Gets the combined standard deviation of the last 50 clicks.
                        if (stdDeviations.isFull()) {
                            double stdDevCombined = MathUtil.getStandardDeviation(stdDeviations);

                            // This is a very low standard deviation that only appears when using
                            // an auto-clicker with a very limited range (e.g. 8-10 CPS).
                            // Any good auto-clicker with a range of four or more numbers will not be detected by this.
                            if (stdDevCombined < 0.40) { // TODO: Test this against clicking with a mouse; repeatedly.
                                flag(false, "stdDevCombined=" + stdDevCombined);
                            }

                            stdDeviations.clear();
                        }

                        flyingCountQueue.clear();
                    }
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
