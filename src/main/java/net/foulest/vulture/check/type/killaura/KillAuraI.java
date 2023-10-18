package net.foulest.vulture.check.type.killaura;

import com.google.common.collect.Lists;
import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import lombok.NonNull;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.MathUtil;
import org.bukkit.Location;

import java.util.Deque;

@CheckInfo(name = "KillAura (I)", type = CheckType.KILLAURA)
public class KillAuraI extends Check {

    private final Deque<Float> samples = Lists.newLinkedList();
    private double buffer;

    public KillAuraI(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
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

                if (deltaYaw > 0.0 && deltaPitch > 0.0 && !Double.isNaN(deltaYaw % deltaPitch)) {
                    samples.add(deltaYaw % deltaPitch);
                }

                if (samples.size() == 20) {
                    int distinct = (int) (samples.stream().distinct().count());
                    int duplicates = samples.size() - distinct;
                    double average = samples.stream().mapToDouble(d -> d).average().orElse(0.0);
                    double stdDev = MathUtil.getStandardDeviation(samples);

                    if (duplicates < 2 && average > 0.45 && average < 0.61 && stdDev > 3.0 && stdDev < 4.0) {
                        if (++buffer > 3.5) {
                            flag("average=" + average
                                    + " duplicates=" + duplicates
                                    + " stdDev=" + stdDev);
                        }
                    } else {
                        buffer = Math.max(buffer - 1, 0);
                    }

                    samples.clear();
                }
            }
        }
    }
}
