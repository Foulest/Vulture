package net.foulest.vulture.processor.type;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.event.impl.PacketPlaySendEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.out.abilities.WrappedPacketOutAbilities;
import io.github.retrooper.packetevents.packetwrappers.play.out.animation.WrappedPacketOutAnimation;
import io.github.retrooper.packetevents.packetwrappers.play.out.camera.WrappedPacketOutCamera;
import io.github.retrooper.packetevents.packetwrappers.play.out.entity.WrappedPacketOutEntity;
import io.github.retrooper.packetevents.packetwrappers.play.out.entitydestroy.WrappedPacketOutEntityDestroy;
import io.github.retrooper.packetevents.packetwrappers.play.out.entityteleport.WrappedPacketOutEntityTeleport;
import io.github.retrooper.packetevents.packetwrappers.play.out.entityvelocity.WrappedPacketOutEntityVelocity;
import io.github.retrooper.packetevents.packetwrappers.play.out.namedentityspawn.WrappedPacketOutNamedEntitySpawn;
import io.github.retrooper.packetevents.packetwrappers.play.out.position.WrappedPacketOutPosition;
import io.github.retrooper.packetevents.packetwrappers.play.out.resourcepacksend.WrappedPacketOutResourcePackSend;
import io.github.retrooper.packetevents.packetwrappers.play.out.transaction.WrappedPacketOutTransaction;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.ping.PingTask;
import net.foulest.vulture.processor.Processor;
import net.foulest.vulture.util.BlockUtil;
import net.foulest.vulture.util.data.CustomLocation;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.foulest.vulture.util.MessageUtil.debug;

/**
 * Handles all outgoing packets before they are encoded.
 *
 * @author Foulest
 * @project Vulture
 */
public class PacketSendProcessor extends Processor {

    /**
     * Handles outgoing packets before they are encoded.
     *
     * @param event The packet event.
     */
    @Override
    public void onPacketPlaySend(@NotNull PacketPlaySendEvent event) {
        // Ignores invalid outgoing packets.
        if (PacketType.getPacketFromId(event.getPacketId()) == null) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        switch (event.getPacketId()) {
            case PacketType.Play.Server.ABILITIES:
                WrappedPacketOutAbilities abilities = new WrappedPacketOutAbilities(event.getNMSPacket());

                playerData.setFlying(abilities.isFlying());
                playerData.setFlightAllowed(abilities.isFlightAllowed());
                playerData.setFlySpeed(abilities.getFlySpeed());
                playerData.setWalkSpeed(abilities.getWalkSpeed());
                playerData.setInstantBuild(abilities.canBuildInstantly());
                playerData.setVulnerable(abilities.isVulnerable());

                if (abilities.isFlying()) {
                    playerData.setTimestamp(ActionType.START_FLYING);
                } else {
                    playerData.setTimestamp(ActionType.STOP_FLYING);
                }
                break;

            case PacketType.Play.Server.ANIMATION:
                WrappedPacketOutAnimation animation = new WrappedPacketOutAnimation(event.getNMSPacket());

                if (animation.getAnimationType() == WrappedPacketOutAnimation.EntityAnimationType.LEAVE_BED) {
                    playerData.setInBed(false);
                }
                break;

            case PacketType.Play.Server.ATTACH_ENTITY:
                if (player.isInsideVehicle()) {
                    playerData.setTimestamp(ActionType.ENTER_VEHICLE);
                } else {
                    playerData.setTimestamp(ActionType.LEAVE_VEHICLE);
                }
                break;

            case PacketType.Play.Server.RESPAWN:
                playerData.setSprinting(false);
                playerData.setSneaking(false);
                playerData.setTimestamp(ActionType.RESPAWN);
                break;

            case PacketType.Play.Server.CAMERA:
                WrappedPacketOutCamera camera = new WrappedPacketOutCamera(event.getNMSPacket());
                playerData.setInCamera(camera.getEntity() != null);
                break;

            case PacketType.Play.Server.CLOSE_WINDOW:
                playerData.setInventoryOpen(false);
                break;

            case PacketType.Play.Server.ENTITY_VELOCITY:
                WrappedPacketOutEntityVelocity entityVelocity = new WrappedPacketOutEntityVelocity(event.getNMSPacket());
                Vector3d velocity = entityVelocity.getVelocity();

                if (entityVelocity.getEntityId() == player.getEntityId()) {
                    playerData.setLastVelocityX(playerData.getVelocityX());
                    playerData.setLastVelocityY(playerData.getVelocityY());
                    playerData.setLastVelocityZ(playerData.getVelocityZ());
                    playerData.setLastVelocityXZ(playerData.getVelocityXZ());

                    playerData.setVelocityX(velocity.getX());
                    playerData.setVelocityY(velocity.getY());
                    playerData.setVelocityZ(velocity.getZ());
                    playerData.setVelocityXZ(Math.hypot(velocity.getX(), velocity.getZ()));

                    playerData.setVelocityTicks(playerData.getTotalTicks());
                    playerData.setTimestamp(ActionType.VELOCITY_GIVEN);
                }
                break;

            case PacketType.Play.Server.RESOURCE_PACK_SEND:
                WrappedPacketOutResourcePackSend resourcePackSend = new WrappedPacketOutResourcePackSend(event.getNMSPacket());
                String url = resourcePackSend.getUrl();
                String scheme = URI.create(url).getScheme();

                if (scheme == null) {
                    event.setCancelled(true);
                    debug("ResourcePackSend packet cancelled; contained null URI scheme");
                    break;
                }

                if (!scheme.equals("https") && !scheme.equals("http") && !scheme.equals("level")) {
                    event.setCancelled(true);
                    debug("ResourcePackSend packet cancelled; contained invalid URI scheme");
                    break;
                }

                try {
                    url = URLDecoder.decode(url.substring("level://".length()), StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException ignored) {
                    event.setCancelled(true);
                    debug("ResourcePackSend packet cancelled; could not decode URL");
                    break;
                }

                if (scheme.equals("level") && (url.contains("..") || !url.endsWith("/resources.zip"))) {
                    event.setCancelled(true);
                    debug("ResourcePackSend packet cancelled; contained invalid level URL");
                    break;
                }
                break;

            case PacketType.Play.Server.TRANSACTION:
                WrappedPacketOutTransaction transaction = new WrappedPacketOutTransaction(event.getNMSPacket());
                playerData.getTransactionSentMap().put(transaction.getActionNumber(), System.currentTimeMillis());
                break;

            case PacketType.Play.Server.OPEN_WINDOW:
                // Players can't open their inventory in portals.
                if (playerData.isNearPortal()) {
                    break;
                }

                playerData.setInventoryOpen(true);
                playerData.setTimestamp(ActionType.INVENTORY_OPEN);
                break;

            case PacketType.Play.Server.POSITION:
                WrappedPacketOutPosition position = new WrappedPacketOutPosition(event.getNMSPacket());
                Location teleportLoc = new Location(player.getWorld(), position.getPosition().getX(),
                        position.getPosition().getY(), position.getPosition().getZ(),
                        position.getYaw(), position.getPitch());

                playerData.setLastServerPositionTick(0);
                playerData.setLastTeleportPacket(position);
                playerData.setTimestamp(ActionType.TELEPORT);

                if (BlockUtil.isLocationInUnloadedChunk(teleportLoc)) {
                    playerData.setInUnloadedChunk(true);
                    playerData.setTimestamp(ActionType.IN_UNLOADED_CHUNK);
                }

                CustomLocation loc = new CustomLocation(
                        position.getPosition().getX(), position.getPosition().getY(), position.getPosition().getZ(),
                        position.getYaw(), position.getPitch()
                );

                // These packets can be received outside the tick start and end interval
                if (playerData.getPingTaskScheduler().isStarted()) {
                    playerData.getPingTaskScheduler().scheduleTask(PingTask.start(() -> playerData.getTeleports().add(loc)));
                } else {
                    playerData.getTeleports().add(loc);
                }
                break;

            case PacketType.Play.Server.NAMED_ENTITY_SPAWN:
                WrappedPacketOutNamedEntitySpawn namedEntitySpawn = new WrappedPacketOutNamedEntitySpawn(event.getNMSPacket());

                if (namedEntitySpawn.getEntity().getType() == EntityType.PLAYER) {
                    playerData.getPingTaskScheduler().scheduleTask(
                            PingTask.start(
                                    () -> playerData.getEntityTracker().addEntity(namedEntitySpawn.getEntityId(), namedEntitySpawn.getPosition().x, namedEntitySpawn.getPosition().y, namedEntitySpawn.getPosition().z)
                            )
                    );
                }
                break;

            case PacketType.Play.Server.REL_ENTITY_MOVE:
                WrappedPacketOutEntity.WrappedPacketOutRelEntityMove move = new WrappedPacketOutEntity.WrappedPacketOutRelEntityMove(event.getNMSPacket());

                playerData.getPingTaskScheduler().scheduleTask(
                        PingTask.of(
                                () -> playerData.getEntityTracker().moveEntity(move.getEntityId(), move.getDeltaX(), move.getDeltaY(), move.getDeltaZ()),
                                () -> playerData.getEntityTracker().markCertain(move.getEntityId())
                        )
                );
                break;

            case PacketType.Play.Server.REL_ENTITY_MOVE_LOOK:
                WrappedPacketOutEntity.WrappedPacketOutRelEntityMoveLook moveLook = new WrappedPacketOutEntity.WrappedPacketOutRelEntityMoveLook(event.getNMSPacket());

                playerData.getPingTaskScheduler().scheduleTask(
                        PingTask.of(
                                () -> playerData.getEntityTracker().moveEntity(moveLook.getEntityId(), moveLook.getDeltaX(), moveLook.getDeltaY(), moveLook.getDeltaZ()),
                                () -> playerData.getEntityTracker().markCertain(moveLook.getEntityId())
                        )
                );
                break;

            case PacketType.Play.Server.ENTITY_TELEPORT:
                WrappedPacketOutEntityTeleport teleport = new WrappedPacketOutEntityTeleport(event.getNMSPacket());

                playerData.getPingTaskScheduler().scheduleTask(
                        PingTask.of(
                                () -> playerData.getEntityTracker().teleportEntity(teleport.getEntityId(), teleport.getPosition().x, teleport.getPosition().y, teleport.getPosition().z),
                                () -> playerData.getEntityTracker().markCertain(teleport.getEntityId())
                        )
                );
                break;

            case PacketType.Play.Server.ENTITY_DESTROY:
                WrappedPacketOutEntityDestroy destroy = new WrappedPacketOutEntityDestroy(event.getNMSPacket());

                playerData.getPingTaskScheduler().scheduleTask(
                        PingTask.start(() -> Arrays.stream(destroy.getEntityIds()).forEach(playerData.getEntityTracker()::removeEntity))
                );
                break;

            default:
                break;
        }

        // Handles packet checks.
        handlePacketChecks(playerData, event);
    }

    /**
     * Handle the checks for the given packet event.
     *
     * @param playerData The player data.
     * @param event      The packet event.
     */
    private void handlePacketChecks(@NotNull PlayerData playerData,
                                    @NotNull CancellableNMSPacketEvent event) {
        long timestamp = System.currentTimeMillis();
        NMSPacket nmsPacket = event.getNMSPacket();

        // Create a copy of the checks list to avoid ConcurrentModificationException
        if (playerData.getChecks() != null) {
            List<Check> checksCopy = new ArrayList<>(playerData.getChecks());

            for (Check check : checksCopy) {
                if (check.getCheckInfo().acceptsServerPackets()) {
                    check.handle(event, event.getPacketId(), nmsPacket, nmsPacket.getRawNMSPacket(), timestamp);
                }
            }
        }
    }
}
