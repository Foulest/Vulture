package net.foulest.vulture.data;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.packetwrappers.play.out.position.WrappedPacketOutPosition;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.Getter;
import lombok.NonNull;
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
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

@Getter
@Setter
public class PlayerData {

    // Player data
    private UUID uniqueId;
    private Player player;
    private ClientVersion version = ClientVersion.UNKNOWN;
    @Getter
    private final List<Check> checks = new ArrayList<>();

    // Timestamps
    private Map<ActionType, Long> actionTimestamps = new HashMap<>();
    private long transPing;

    // Ticks
    private int totalTicks = 0;
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
    private int velocityH;
    private int velocityTicks;
    private int velocityV;

    // Packets
    private WrappedPacketOutPosition lastTeleportPacket;
    private WrappedPacketInFlying lastRotationPacket;
    private WrappedPacketInFlying lastPositionPacket;

    // Other data
    private boolean alertsEnabled = false;
    private boolean kicking = false;
    private boolean newViolationsPaused = false;
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
    private double attackerX, attackerY, attackerZ;
    private double attackerX2, attackerY2, attackerZ2;
    private float attackerYaw, attackerPitch;
    private float attackerYaw2, attackerPitch2;

    // Target data
    private Observable<Player> target = new Observable<>(null);
    private Player lastLastTarget;
    private Player lastTarget;

    // Velocity data
    private double lastVelocityX;
    private double lastVelocityY;
    private double lastVelocityZ;
    private double velocityHorizontal;
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
    private boolean nearPiston;
    private boolean nearCactus;
    private boolean inWeb;
    private boolean inLiquid;
    private boolean nearLiquid;
    private boolean onChest;
    private boolean onClimbable;
    private boolean nearClimbable;
    private boolean onSnowLayer;
    private boolean onIce;
    private boolean onSoulSand;
    private boolean nearTrapdoor;
    private boolean nearFenceGate;
    private boolean nearLilyPad;
    private boolean nearAnvil;
    private boolean nearSlimeBlock;
    private boolean underBlock;
    private boolean againstBlock;
    private boolean isInsideBlock;
    private Block collidingBlock;
    private boolean inCamera;

    // Abilities packet
    private boolean flying;
    private boolean flightAllowed;
    private boolean instantBuild;
    private boolean vulnerable;
    private float flySpeed;
    private float walkSpeed;

    // Other packets
    private boolean eating = false;
    private boolean drinking = false;

    // Animation packet
    private boolean inBed = false;

    // BlockDig packet
    private boolean digging = false;

    // BlockPlace packet
    private boolean blocking = false;
    private boolean shootingBow = false;
    private boolean placingBlock = false;

    // EntityAction packet
    private boolean sprinting = false;
    private boolean sneaking = false;
    private boolean inventoryOpen = false;

    // HeldItemSlot packet
    private int currentSlot = -1;

    public PlayerData(UUID uniqueId, Player player) {
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
    public static void updateFlyingLocations(@NonNull PlayerData data,
                                             @NonNull WrappedPacketInFlying flying,
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
}
