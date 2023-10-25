package net.foulest.vulture.check.type.timer;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.MathUtil;
import net.foulest.vulture.util.data.EvictingList;

@CheckInfo(name = "Timer (A)", type = CheckType.TIMER, maxViolations = 25,
        acceptsServerPackets = true, description = "Detects modifying your game speed.")
public class TimerA extends Check {

    private final EvictingList<Long> flyingDiffs = new EvictingList<>(40);
    private long lastFlyingPacket = 0;
    private double buffer;

    public TimerA(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull CancellableNMSPacketEvent event, byte packetId,
                       @NonNull NMSPacket nmsPacket, @NonNull Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            long timeSinceLag = playerData.getTimeSince(ActionType.LAG);
            long transPing = playerData.getTransPing();

            int totalTicks = playerData.getTotalTicks();
            int lastDroppedPackets = playerData.getLastDroppedPackets();

            if (totalTicks < 300 && timeSinceLag > 100 && totalTicks - lastDroppedPackets < 5) {
                return;
            }

            // Detecting modifying your game speed to be faster than it should be is easy.
            // We can just check the time between Flying packets and compare it to the average.
            // Checking if your game speed is *slower* than it should be is nearly impossible.

            // When a player reloads their textures or freezes their game, the game speed will briefly
            // be slower than it should be and gradually come back to its normal speed. When players
            // repeatedly reload textures, game speed can be as slow as 60%. This is ridiculous.

            if (lastFlyingPacket != 0) {
                flyingDiffs.add(System.currentTimeMillis() - lastFlyingPacket);
                double average = flyingDiffs.stream().mapToDouble(d -> d).average().orElse(0.0);
                double speed = 50 / average;
                double modifier = MathUtil.getPingToTimer(transPing + 50);

                // Detects Timer speeds of 101.0% or higher.
                if (flyingDiffs.isFull()) {
                    if (speed >= (1.01 + modifier)) {
                        if (++buffer > 50) {
                            KickUtil.kickPlayer(player, event, "Timer (A)");
                        }
                    } else {
                        buffer = Math.max(buffer - 0.9, 0.0);
                    }
                }
            }

            lastFlyingPacket = System.currentTimeMillis();

        } else if (packetId == PacketType.Play.Server.POSITION) {
            // Compensates for teleports.
            flyingDiffs.add(150L);
        }
    }
}
