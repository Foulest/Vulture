/*
 * Vulture - a server protection plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.vulture.data;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import lombok.Data;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.Violation;
import net.foulest.vulture.check.type.clientbrand.type.ModType;
import net.foulest.vulture.check.type.clientbrand.type.PayloadType;
import net.foulest.vulture.ping.PingTask;
import net.foulest.vulture.ping.PingTaskScheduler;
import net.foulest.vulture.timing.Timing;
import net.foulest.vulture.util.data.CustomLocation;
import net.foulest.vulture.util.data.EvictingList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Data
public class PlayerData {

    // Player data
    private UUID uniqueId;
    private Player player;
    private @NotNull ClientVersion version = ClientVersion.UNKNOWN;

    // Protection data
    private final List<Check> checks = new ArrayList<>();
    private boolean alertsEnabled;
    private boolean verboseEnabled;
    private boolean newViolationsPaused;
    private @NotNull List<Violation> violations = new ArrayList<>();

    // Payload data
    private @NotNull List<PayloadType> payloads = new ArrayList<>();
    private @NotNull List<ModType> mods = new ArrayList<>();

    // Packet counts
    private int ticksBeforeReset;
    private int packetsSentPerTick;
    private int packetsSentPerSecond;
    private final EvictingList<Integer> smoothedSentPerSecond = new EvictingList<>(5);
    private final Map<Integer, Integer> packetCounts = new HashMap<>();

    // Timestamps
    private @NotNull Map<ActionType, Integer> actionTimestamps = new EnumMap<>(ActionType.class);

    // Ticks
    private int totalTicks;
    private int lastDroppedPackets;
    private int lastFlyingTicks;

    // Packets
    private WrapperPlayServerPlayerPositionAndLook lastTeleportPacket;
    private WrapperPlayClientPlayerFlying lastRotationPacket;
    private WrapperPlayClientPlayerFlying lastPositionPacket;
    private WrapperPlayClientSettings lastSettingsPacket;

    // Transaction data
    private @NotNull Map<Short, Long> transactionSentMap = new HashMap<>();
    private @NotNull Map<Short, Long> transactionTime = new HashMap<>();
    private long transPing;

    // Abilities packet
    private boolean flying;
    private boolean flightAllowed;
    private boolean creativeMode;
    private boolean godMode;

    // Player state information
    private Location location;
    private boolean moving;
    private boolean eating;
    private boolean drinking;
    private boolean digging;
    private boolean blocking;
    private boolean shootingBow;
    private boolean placingBlock;
    private boolean sprinting;
    private boolean sneaking;
    private boolean inventoryOpen;
    private int currentSlot = -1;

    // Dusk
    private final @NotNull PingTaskScheduler pingTaskScheduler;
    private final @NotNull Timing timing;
    private final Queue<CustomLocation> teleports = new ArrayDeque<>();

    public PlayerData(UUID uniqueId, @NotNull Player player) {
        this.uniqueId = uniqueId;
        this.player = player;
        pingTaskScheduler = new PingTaskScheduler();
        timing = new Timing(player, System.currentTimeMillis());
        location = player.getLocation();
        alertsEnabled = player.hasPermission("vulture.alerts") || player.isOp();

        for (ActionType action : ActionType.values()) {
            actionTimestamps.put(action, 0);
        }

        setTimestamp(ActionType.LOGIN);
    }

    public void onPingSendStart() {
        pingTaskScheduler.onPingSendStart();
    }

    @SuppressWarnings("NestedMethodCall")
    public void onPingSendEnd() {
        // Increment the total ticks
        totalTicks++;

        // Clears the packet count for the next tick
        packetsSentPerTick = 0;
        packetCounts.clear();

        // Resets the packets sent per second count every second
        ++ticksBeforeReset;
        if (ticksBeforeReset >= 20) {
            smoothedSentPerSecond.add(packetsSentPerSecond);
            packetsSentPerSecond = 0;
            ticksBeforeReset = 0;
        }

        // First schedule the timing synchronization task
        long time = System.currentTimeMillis();
        pingTaskScheduler.scheduleTask(PingTask.end(() -> timing.ping(time)));
        pingTaskScheduler.onPingSendEnd();
    }

    public void onPongReceiveStart() {
        pingTaskScheduler.onPongReceiveStart();
    }

    public void onPongReceiveEnd() {
        pingTaskScheduler.onPongReceiveEnd();
    }

    public void handlePlayerPacket(@NotNull CustomLocation location) {
        // Handle teleports separately
        if (handleTeleport(location)) {
            return;
        }

        // Tick timing
        timing.tick();
    }

    // Only for client responses to teleports
    private boolean handleTeleport(@NotNull CustomLocation location) {
        CustomLocation teleport = teleports.peek();

        if (location.equals(teleport)) {
            teleports.poll();
            return true;
        }
        return false;
    }

    /**
     * Gets a timestamp for an action.
     *
     * @param action Action to get the timestamp for.
     * @return Timestamp for the action.
     */
    public int getTimestamp(ActionType action) {
        return actionTimestamps.getOrDefault(action, 0);
    }

    /**
     * Sets a timestamp for an action.
     *
     * @param action Action to set the timestamp for.
     */
    public void setTimestamp(ActionType action) {
        actionTimestamps.put(action, totalTicks);
    }

    /**
     * Gets the ticks since an action occurred.
     * One tick is 50ms.
     *
     * @param action Action to get the ticks since.
     * @return Ticks since the action occurred.
     */
    public int getTicksSince(ActionType action) {
        return totalTicks - actionTimestamps.getOrDefault(action, 0);
    }

    /**
     * Checks if the player is teleporting.
     *
     * @param toPosition Position to check.
     * @return If the player is teleporting.
     */
    public boolean isTeleporting(@NotNull Vector3d toPosition) {
        Vector3d lastTeleportPosition = lastTeleportPacket != null ? lastTeleportPacket.getPosition() : null;

        return lastTeleportPacket != null && lastTeleportPosition != null
                && lastTeleportPosition.getX() == toPosition.getX()
                && lastTeleportPosition.getY() == toPosition.getY()
                && lastTeleportPosition.getZ() == toPosition.getZ();
    }

    public int incrementPacketsSentPerTick() {
        ++packetsSentPerTick;
        return packetsSentPerTick;
    }

    public int incrementPacketsSentPerSecond() {
        ++packetsSentPerSecond;
        return packetsSentPerSecond;
    }

    public int getSmoothedPacketsPerSecond() {
        return (int) smoothedSentPerSecond.stream().mapToInt(Integer::intValue).average().orElse(0);
    }
}
