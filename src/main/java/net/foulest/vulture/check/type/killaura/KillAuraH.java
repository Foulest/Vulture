package net.foulest.vulture.check.type.killaura;

import com.google.common.collect.Lists;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MathUtil;
import org.bukkit.Location;

import java.util.Deque;

@CheckInfo(name = "KillAura (H)", type = CheckType.KILLAURA)
public class KillAuraH extends Check {

    private final Deque<Float> samples = Lists.newLinkedList();
    private double buffer;
    private double buffer2;

    public KillAuraH(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        // Checks the player for exemptions.
        if (playerData.getLastAttackTick() > 2) {
            return;
        }

        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);

            if (flying.isRotating()) {
                Location to = new Location(player.getWorld(), flying.getPosition().getX(), flying.getPosition().getY(),
                        flying.getPosition().getZ(), flying.getYaw(), flying.getPitch());
                Location from = playerData.getLastLocation();

                float deltaYaw = Math.abs(to.getYaw() - from.getYaw());
                float deltaPitch = Math.abs(to.getPitch() - from.getPitch());

                if (deltaYaw > 0.0 && deltaPitch > 0.0) {
                    samples.add(deltaYaw % deltaPitch);
                }

                if (samples.size() == 40) {
                    int distinct = (int) (samples.stream().distinct().count());
                    int duplicates = samples.size() - distinct;
                    double average = samples.stream().mapToDouble(d -> d).average().orElse(0.0);
                    double stdDev = MathUtil.getStandardDeviation(samples);

                    if (duplicates >= 6 && average > 0.125 && average < 0.55 && stdDev > 0.165 && stdDev < 0.325) {
                        if (++buffer > 2.25) {
                            flag(false, "A"
                                    + " (average=" + average
                                    + " duplicates=" + duplicates
                                    + " stdDev=" + stdDev + ")");
                        }
                    } else {
                        buffer = Math.max(buffer - 0.25, 0);
                    }

                    if ((duplicates >= 4 && average > 0.3 && average < 0.6 && stdDev > 0.3 && stdDev < 0.45)
                            || (duplicates > 2 && Math.abs(stdDev - average) < 0.011)) {
                        if (++buffer2 > 2.5) {
                            flag(false, "B"
                                    + " (average=" + average
                                    + " duplicates=" + duplicates
                                    + " stdDev=" + stdDev + ")");
                        }
                    } else {
                        buffer2 = Math.max(buffer2 - 0.25, 0);
                    }

                    samples.clear();
                }
            }
        }
    }
}
