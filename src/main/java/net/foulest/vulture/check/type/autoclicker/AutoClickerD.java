package net.foulest.vulture.check.type.autoclicker;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MathUtil;

import java.util.LinkedList;

@CheckInfo(name = "AutoClicker (D)", type = CheckType.AUTOCLICKER)
public class AutoClickerD extends Check {

    public final LinkedList<Integer> clicksList = new LinkedList<>();
    public int flyingCount;
    private double lastStdDev;
    private double buffer;

    public AutoClickerD(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            ++flyingCount;

        } else if (packetId == PacketType.Play.Client.ARM_ANIMATION) {
            if (flyingCount < 10 && !playerData.isPlacingBlock() && !playerData.isDigging()) {
                clicksList.add(flyingCount);

                double stdDev = MathUtil.getStandardDeviation(clicksList);

                if (clicksList.size() == 50) {
                    if (Math.abs(stdDev - lastStdDev) < 0.05) {
                        if (++buffer > 2) {
                            flag("stdDev=" + stdDev
                                    + " lastStdDev=" + lastStdDev);
                        }
                    } else {
                        buffer = Math.max(buffer - 1.25, 0.0);
                    }

                    clicksList.clear();
                    lastStdDev = stdDev;
                }
            }

            flyingCount = 0;
        }
    }
}
