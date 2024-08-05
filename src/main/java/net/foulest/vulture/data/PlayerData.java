/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
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

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.out.position.WrappedPacketOutPosition;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.Violation;
import net.foulest.vulture.check.type.clientbrand.type.PayloadType;
import net.foulest.vulture.ping.PingTask;
import net.foulest.vulture.ping.PingTaskScheduler;
import net.foulest.vulture.timing.Timing;
import net.foulest.vulture.tracking.EntityTracker;
import net.foulest.vulture.util.data.CustomLocation;
import net.foulest.vulture.util.data.EvictingList;
import net.foulest.vulture.util.data.Pair;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@Setter
@ToString
public class PlayerData {

    // Player data
    private UUID uniqueId;
    private Player player;
    private ClientVersion version = ClientVersion.TEMP_UNRESOLVED;

    // Anti-cheat data
    @Getter
    private final List<Check> checks = new ArrayList<>();
    private boolean alertsEnabled;
    private boolean verboseEnabled;
    private boolean newViolationsPaused;
    private List<Violation> violations = new ArrayList<>();
    private List<PayloadType> payloads = new ArrayList<>();

    // Packet counts
    private int ticksBeforeReset;
    private int packetsSentPerTick;
    private int packetsSentPerSecond;
    private final EvictingList<Integer> smoothedSentPerSecond = new EvictingList<>(5);
    private final Map<Byte, Integer> packetCounts = new HashMap<>();

    // Timestamps
    private Map<ActionType, Integer> actionTimestamps = new EnumMap<>(ActionType.class);
    private long transPing;

    // Ticks
    private int totalTicks;
    private int aboveBlockTicks;
    private int aboveBlockTicksStrict;
    private int underBlockTicks;
    private int airTicks;
    private int airTicksStrict;
    private int groundTicks;
    private int groundTicksStrict;
    private int lastAttackTick;
    private int lastDroppedPackets;
    private int lastFlyingTicks;
    private int lastServerPositionTick;
    private int velocityTicks;

    // Packets
    private WrappedPacketOutPosition lastTeleportPacket;
    private WrappedPacketInFlying lastRotationPacket;
    private WrappedPacketInFlying lastPositionPacket;

    // Reach data
    private Map<Short, Long> transactionSentMap = new HashMap<>();
    private Map<Short, Long> transactionTime = new HashMap<>();
    private Location lastOnGroundLocation;
    private Location lastLastLastLocation;
    private Location lastLastLocation;
    private Location lastLocation;
    private Location location;
    private boolean teleportReset;

    // Attacking data
    private boolean attacking;
    private int lastAttacked;

    // Velocity check data
    private Pair<Integer, Double> velocityY = new Pair<>(0, 0.0);
    private Pair<Integer, Double> velocityXZ = new Pair<>(0, 0.0);
    private Pair<Integer, Double> lastVelocityY = new Pair<>(0, 0.0);
    private Pair<Integer, Double> lastVelocityXZ = new Pair<>(0, 0.0);

    // Block data
    private boolean moving;
    private boolean nearGround;
    private boolean onGround;
    private boolean onSlab;
    private boolean onStairs;
    private boolean nearStairs;
    private boolean nearSlab;
    private boolean nearPiston;
    private boolean nearCactus;
    private boolean inWeb;
    private boolean inLiquid;
    private boolean nearLiquid;
    private boolean onChest;
    private boolean onClimbable;
    private boolean nearClimbable;
    private boolean nearPortal;
    private boolean onSnowLayer;
    private boolean onIce;
    private boolean onSoulSand;
    private boolean nearTrapdoor;
    private boolean nearFenceGate;
    private boolean onLilyPad;
    private boolean nearLilyPad;
    private boolean nearAnvil;
    private boolean nearSlimeBlock;
    private boolean underBlock;
    private boolean againstBlock;
    private boolean againstBlockWide;
    private boolean isInsideBlock;
    private Block collidingBlock;
    private boolean inCamera;
    private boolean inUnloadedChunk;
    private boolean touchedGroundSinceLogin;
    private boolean underEffectOfSlime;

    // Abilities packet
    private boolean flying;
    private boolean flightAllowed;
    private boolean instantBuild;
    private boolean vulnerable;

    // Other packets
    private boolean eating;
    private boolean drinking;

    // Animation packet
    private boolean inBed;

    // BlockDig packet
    private boolean digging;

    // BlockPlace packet
    private boolean blocking;
    private boolean shootingBow;
    private boolean placingBlock;

    // EntityAction packet
    private boolean sprinting;
    private boolean sneaking;
    private boolean wasSneaking;
    private boolean inventoryOpen;

    // HeldItemSlot packet
    private int currentSlot = -1;

    // Dusk
    private final EntityTracker entityTracker;
    private final PingTaskScheduler pingTaskScheduler;
    private final Timing timing;
    private final CustomLocation lastLoc = new CustomLocation();
    private final CustomLocation loc = new CustomLocation();
    private final Queue<CustomLocation> teleports = new ArrayDeque<>();
    private boolean accuratePosition;
    private boolean hasPosition;

    public PlayerData(UUID uniqueId, @NotNull Player player) {
        this.uniqueId = uniqueId;
        this.player = player;
        entityTracker = new EntityTracker();
        pingTaskScheduler = new PingTaskScheduler();
        timing = new Timing(player, System.currentTimeMillis());

        location = player.getLocation();
        lastLocation = location;
        lastLastLocation = lastLocation;
        lastLastLastLocation = lastLastLocation;
        lastOnGroundLocation = location;

        for (ActionType action : ActionType.values()) {
            actionTimestamps.put(action, 0);
        }

        setTimestamp(ActionType.LOGIN);
    }

    public void onPingSendStart() {
        pingTaskScheduler.onPingSendStart();
    }

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

    public void handlePlayerPacket(CustomLocation location) {
        // Handle teleports separately
        if (handleTeleport(location)) {
            return;
        }

        preTick();

        if (location.hasPos()) {
            loc.setPos(location.getPos());
        }

        if (location.hasRot()) {
            loc.setRot(location.getRot());
        }

        hasPosition = location.hasPos();

        tick();
        postTick();
    }

    // Called before the tick runs, only used for setting previous locations here
    private void preTick() {
        lastLoc.set(loc);
    }

    // Called after the tick has been completed on the client
    private void tick() {
        if (attacking) {
            attacking = false;
        }

        // Interpolating tracked entities is after attacking in the client tick
        entityTracker.interpolate();

        // Tick timing
        timing.tick();
    }

    // Called after tick runs
    private void postTick() {
        wasSneaking = sneaking;
        accuratePosition = hasPosition;
    }

    // Only for client responses to teleports
    private boolean handleTeleport(@NotNull CustomLocation location) {
        CustomLocation teleport = teleports.peek();

        if (location.equals(teleport)) {
            teleports.poll();
            loc.set(teleport);
            accuratePosition = true; // Position from last tick is no longer inaccurate
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
    public boolean isTeleporting(io.github.retrooper.packetevents.utils.vector.Vector3d toPosition) {
        io.github.retrooper.packetevents.utils.vector.Vector3d lastTeleportPosition = lastTeleportPacket != null ? lastTeleportPacket.getPosition() : null;

        return lastTeleportPacket != null && lastTeleportPosition != null
                && lastTeleportPosition.getX() == toPosition.getX()
                && lastTeleportPosition.getY() == toPosition.getY()
                && lastTeleportPosition.getZ() == toPosition.getZ();
    }

    /**
     * Checks if the player is near a boat.
     *
     * @param x The X radius to check.
     * @param y The Y radius to check.
     * @param z The Z radius to check.
     * @return If the player is near a boat.
     */
    public boolean isNearbyBoat(double x, double y, double z) {
        for (Entity entity : player.getNearbyEntities(x, y, z)) {
            if (entity.getType() == EntityType.BOAT) {
                return true;
            }
        }
        return false;
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
