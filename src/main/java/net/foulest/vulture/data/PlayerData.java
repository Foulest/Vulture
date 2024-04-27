package net.foulest.vulture.data;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.out.position.WrappedPacketOutPosition;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.Violation;
import net.foulest.vulture.check.type.clientbrand.type.PayloadType;
import net.foulest.vulture.ping.PingTask;
import net.foulest.vulture.ping.PingTaskScheduler;
import net.foulest.vulture.timing.Timing;
import net.foulest.vulture.tracking.EntityTracker;
import net.foulest.vulture.tracking.EntityTrackerEntry;
import net.foulest.vulture.util.Constants;
import net.foulest.vulture.util.MathUtil;
import net.foulest.vulture.util.data.Area;
import net.foulest.vulture.util.data.CustomLocation;
import net.foulest.vulture.util.data.EvictingList;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.*;
import java.util.stream.Stream;

@Getter
@Setter
public class PlayerData {

    // Player data
    private UUID uniqueId;
    private Player player;
    private ClientVersion version = ClientVersion.TEMP_UNRESOLVED;

    // Anti-cheat data
    @Getter
    private final List<Check> checks = new ArrayList<>();
    private boolean alertsEnabled = false;
    private boolean verboseEnabled = false;
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
    private Map<ActionType, Integer> actionTimestamps = new HashMap<>();
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

    // Sensitivity data
    private int sensitivity;
    private float sensitivityY;
    private float smallestRotationGCD;
    private float pitchGCD;

    // Cinematic data
    private boolean cinematic;
    private float predictYaw;
    private float predictPitch;

    // Velocity check data
    // This data is retrieved a tick later than the other data.
    private double lastVelocityX;
    private double lastVelocityY;
    private double lastVelocityZ;
    private double lastVelocityXZ;
    private double velocityXZ;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    // Block data
    private boolean moving;
    private boolean nearGround;
    private boolean onGround;
    private boolean onSlab;
    private boolean onStairs;
    private boolean nearStairs;
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
    private float flySpeed;
    private float walkSpeed;

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
    private final CustomLocation locO = new CustomLocation();
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
        if (++ticksBeforeReset >= 20) {
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
        locO.set(loc);
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

    // Tries to mirror client logic as closely as possible while including a few errors
    public Optional<Double> performReachCheck(@NotNull EntityTrackerEntry entry) {
        // Get position area of entity we have been tracking
        Area position = entry.getPosition();

        // Expand position area into bounding box
        float width = Constants.PLAYER_BOX_WIDTH;
        float height = Constants.PLAYER_BOX_HEIGHT;

        Area box = new Area(position)
                .expand(width / 2.0, 0.0, width / 2.0)
                .addCoord(0.0, height, 0.0);

        // The hitbox is actually 0.1 blocks bigger than the bounding box
        float offset = Constants.COLLISION_BORDER_SIZE;
        box.expand(offset, offset, offset);

        // Compensate for fast math errors in the look vector calculations (Can remove if support not needed)
        double error = Constants.FAST_MATH_ERROR;
        box.expand(error, error, error);

        /*
        Expand the box by the root of the minimum move amount in each axis if the player was not moving the last tick.
        This is because they could have moved this amount on the client making a difference between a hit or miss.
         */
        if (!accuratePosition) {
            double minMove = Constants.MIN_MOVE_UPDATE_ROOT;
            box.expand(minMove, minMove, minMove);
        }

        // Mouse input is done before any sneaking updates
        float eyeHeight = 1.62F;
        if (wasSneaking) {
            eyeHeight -= 0.08F;
        }

        // Previous position since movement is done after attacking in the client tick
        Vector3d eye = locO.getPos().add(0, eyeHeight, 0, new Vector3d());

        // First check if the eye position is inside
        if (box.isInside(eye.x, eye.y, eye.z)) {
            return Optional.of(0.0D);
        }

        // Originally Minecraft uses the old yaw value for mouse intercepts, but some clients and mods fix this
        float yawO = locO.getRot().x;
        float yaw = loc.getRot().x;
        float pitch = loc.getRot().y;

        Vector3d viewO = MathUtil.getLookVector(yawO, pitch).mul(Constants.RAY_LENGTH);
        Vector3d view = MathUtil.getLookVector(yaw, pitch).mul(Constants.RAY_LENGTH);

        Vector3d eyeViewO = eye.add(viewO, new Vector3d());
        Vector3d eyeView = eye.add(view, new Vector3d());

        // Calculate intercepts with Minecraft ray logic
        Vector3d interceptO = MathUtil.calculateIntercept(box, eye, eyeViewO);
        Vector3d intercept = MathUtil.calculateIntercept(box, eye, eyeView);

        // Get minimum value of intercepts
        return Stream.of(interceptO, intercept)
                .filter(Objects::nonNull)
                .map(eye::distance)
                .min(Double::compare);
    }

    /**
     * Gets a timestamp for an action.
     *
     * @param action Action to get the timestamp for.
     * @return Timestamp for the action.
     */
    public long getTimestamp(ActionType action) {
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
        return ++packetsSentPerTick;
    }

    public int incrementPacketsSentPerSecond() {
        return ++packetsSentPerSecond;
    }

    public int getSmoothedPacketsPerSecond() {
        return (int) smoothedSentPerSecond.stream().mapToInt(Integer::intValue).average().orElse(0);
    }
}
