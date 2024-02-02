package net.foulest.vulture.data;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.out.position.WrappedPacketOutPosition;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.Violation;
import net.foulest.vulture.check.type.clientbrand.type.PayloadType;
import net.foulest.vulture.util.data.EvictingList;
import net.foulest.vulture.util.data.Observable;
import net.foulest.vulture.util.data.Pair;
import net.foulest.vulture.util.raytrace.BoundingBox;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;

@Getter
@Setter
public class PlayerData {

    // Player data
    private UUID uniqueId;
    private Player player;
    private ClientVersion version = ClientVersion.TEMP_UNRESOLVED;
    @Getter
    private final List<Check> checks = new ArrayList<>();

    // HamsterAPI
    @Getter
    private Object playerConnection;
    @Getter
    private Object networkManager;
    @Getter
    private Channel channel;
    private Class<?> iChatBaseComponentClass;
    private Method toChatBaseComponent;
    private Method sendPacketMethod;
    private boolean setup;
    private boolean injected;

    // Packet Counts
    private final Map<Byte, Integer> packetCounts = new HashMap<>();
    private int packetsSentPerTick;

    // Timestamps
    private Map<ActionType, Long> actionTimestamps = new HashMap<>();
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
    private int lastPacketDrop;
    private int lastPosition2;
    private int lastPosition;
    private int lastServerPositionTick;
    private int lastTeleportReset;
    private int velocityTicks;

    // Packets
    private WrappedPacketOutPosition lastTeleportPacket;
    private WrappedPacketInFlying lastRotationPacket;
    private WrappedPacketInFlying lastPositionPacket;

    // Other data
    private boolean alertsEnabled;
    private boolean newViolationsPaused;
    private int positionCheckerTaskId;
    private List<Violation> violations = new ArrayList<>();
    private List<PayloadType> payloads = new ArrayList<>();

    // Reach data
    private Map<Short, Long> transactionSentMap = new HashMap<>();
    private HashMap<Short, Vector> velocityIds = new HashMap<>();
    private Map<Short, Long> transactionTime = new HashMap<>();
    private Location lastOnGroundLocation;
    private Location lastLastLastLocation;
    private Location lastLastLocation;
    private Location lastLocation;
    private Location location;
    private boolean droppedPackets;
    private boolean lastLastOnGroundPacket;
    private boolean lastOnGround;
    private boolean lastOnGroundPacket;
    private boolean onGroundPacket;
    private boolean teleportReset;

    // Attacker data
    private double attackerX;
    private double attackerY;
    private double attackerZ;
    private double attackerX2;
    private double attackerY2;
    private double attackerZ2;
    private float attackerYaw;
    private float attackerPitch;
    private float attackerYaw2;
    private float attackerPitch2;

    // Target data
    private Observable<Player> target = new Observable<>(null);
    private Player lastLastTarget;
    private Player lastTarget;

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

    // Past locations
    private EvictingList<Pair<BoundingBox, Integer>> pastLocsA = new EvictingList<>(30);
    private EvictingList<Pair<BoundingBox, Integer>> pastLocsB = new EvictingList<>(20);
    private EvictingList<Pair<BoundingBox, Integer>> pastLocsC = new EvictingList<>(20);

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
    private boolean inventoryOpen;

    // HeldItemSlot packet
    private int currentSlot = -1;

    public PlayerData(UUID uniqueId, @NotNull Player player) {
        this.uniqueId = uniqueId;
        this.player = player;

        location = player.getLocation();
        lastLocation = location;
        lastLastLocation = lastLocation;
        lastLastLastLocation = lastLastLocation;
        lastOnGroundLocation = location;

        for (ActionType action : ActionType.values()) {
            actionTimestamps.put(action, 0L);
        }

        setTimestamp(ActionType.LOGIN);
    }

    /**
     * Updates the player's location values for Reach checks.
     *
     * @param data         Player data.
     * @param flying       Flying packet.
     * @param isPrimarySet If the primary location is being set.
     */
    public static void updateFlyingLocations(PlayerData data,
                                             @NotNull WrappedPacketInFlying flying,
                                             boolean isPrimarySet) {
        if (flying.isMoving()) {
            if (isPrimarySet) {
                data.lastPosition = 0;
                data.attackerX = flying.getPosition().getX();
                data.attackerY = flying.getPosition().getY();
                data.attackerZ = flying.getPosition().getZ();
            } else {
                data.lastPosition2 = 0;
                data.attackerX2 = flying.getPosition().getX();
                data.attackerY2 = flying.getPosition().getY();
                data.attackerZ2 = flying.getPosition().getZ();
            }
        }

        if (!flying.isMoving()) {
            if (isPrimarySet) {
                data.lastPosition++;
            } else {
                data.lastPosition2++;
            }
        }

        if (flying.isRotating()) {
            if (isPrimarySet) {
                data.attackerYaw = flying.getYaw();
                data.attackerPitch = flying.getPitch();
            } else {
                data.attackerYaw2 = flying.getYaw();
                data.attackerPitch2 = flying.getPitch();
            }
        }
    }

    /**
     * If the player "has fast", which is needed for a KillAura check.
     *
     * @return If the player "has fast".
     */
    public boolean hasFast() {
        return getTimestamp(ActionType.FLYING_PACKET) != 0L
                && getTimestamp(ActionType.FAST) != 0L
                && getTimestamp(ActionType.FLYING_PACKET) - getTimestamp(ActionType.FAST) < 100L;
    }

    /**
     * Sets a timestamp for an action.
     *
     * @param action Action to set the timestamp for.
     */
    public void setTimestamp(ActionType action) {
        actionTimestamps.put(action, System.currentTimeMillis());
    }

    /**
     * Gets a timestamp for an action.
     *
     * @param action Action to get the timestamp for.
     * @return Timestamp for the action.
     */
    public long getTimestamp(ActionType action) {
        return actionTimestamps.getOrDefault(action, 0L);
    }

    /**
     * Gets the time since an action occurred.
     *
     * @param action Action to get the time since.
     * @return Time since the action occurred.
     */
    public long getTimeSince(ActionType action) {
        return System.currentTimeMillis() - actionTimestamps.getOrDefault(action, 0L);
    }

    /**
     * Checks if the player is teleporting.
     *
     * @param toPosition Position to check.
     * @return If the player is teleporting.
     */
    public boolean isTeleporting(Vector3d toPosition) {
        Vector3d lastTeleportPosition = lastTeleportPacket != null ? lastTeleportPacket.getPosition() : null;

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
}
