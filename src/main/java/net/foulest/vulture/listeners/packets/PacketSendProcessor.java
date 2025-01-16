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
package net.foulest.vulture.listeners.packets;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResourcePackSend;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.ping.PingTask;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.data.CustomLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Handles all outgoing packets sent from the server.
 *
 * @author Foulest
 */
public class PacketSendProcessor extends SimplePacketListenerAbstract {

    public PacketSendProcessor() {
        super(PacketListenerPriority.HIGH);
    }

    /**
     * Handles outgoing packets.
     *
     * @param event The packet event.
     */
    @Override
    @SuppressWarnings("NestedMethodCall")
    public void onPacketPlaySend(@NotNull PacketPlaySendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        String packetName = packetType.getName();
        Player player = event.getPlayer();

        // Ignores outgoing packets for invalid players.
        if (player == null) {
            return;
        }

        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        String playerName = player.getName();

        // Handles PLAYER_ABILITIES packets.
        if (packetType == PacketType.Play.Server.PLAYER_ABILITIES) {
            @NotNull WrapperPlayServerPlayerAbilities abilities = new WrapperPlayServerPlayerAbilities(event);
            boolean abilitiesFlying = abilities.isFlying();
            boolean abilitiesFlightAllowed = abilities.isFlightAllowed();
            boolean abilitiesCreativeMode = abilities.isInCreativeMode();
            boolean abilitiesGodMode = abilities.isInGodMode();

            playerData.setFlying(abilitiesFlying);
            playerData.setFlightAllowed(abilitiesFlightAllowed);
            playerData.setCreativeMode(abilitiesCreativeMode);
            playerData.setGodMode(abilitiesGodMode);

            if (abilitiesFlying) {
                playerData.setTimestamp(ActionType.START_FLYING);
            } else {
                playerData.setTimestamp(ActionType.STOP_FLYING);
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles ATTACH_ENTITY packets.
        if (packetType == PacketType.Play.Server.ATTACH_ENTITY) {
            if (player.isInsideVehicle()) {
                playerData.setTimestamp(ActionType.ENTER_VEHICLE);
            } else {
                playerData.setTimestamp(ActionType.LEAVE_VEHICLE);
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles RESPAWN packets.
        if (packetType == PacketType.Play.Server.RESPAWN) {
            playerData.setSprinting(false);
            playerData.setSneaking(false);
            playerData.setTimestamp(ActionType.RESPAWN);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles CLOSE_WINDOW packets.
        if (packetType == PacketType.Play.Server.CLOSE_WINDOW) {
            playerData.setInventoryOpen(false);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles RESOURCE_PACK_SEND packets.
        if (packetType == PacketType.Play.Server.RESOURCE_PACK_SEND) {
            @NotNull WrapperPlayServerResourcePackSend resourcePackSend = new WrapperPlayServerResourcePackSend(event);
            String url = resourcePackSend.getUrl();
            String scheme = URI.create(url).getScheme();

            if (scheme == null) {
                MessageUtil.debug("Cancelled" + packetName + " for " + playerName + " (contained null URI scheme)");
                event.setCancelled(true);
                return;
            }

            if (!scheme.equals("https") && !scheme.equals("http") && !scheme.equals("level")) {
                MessageUtil.debug("Cancelled" + packetName + " for " + playerName + " (contained invalid URI scheme)");
                event.setCancelled(true);
                return;
            }

            try {
                String utf8 = StandardCharsets.UTF_8.toString();
                int levelLength = "level://".length();
                @NotNull String beginIndex = url.substring(levelLength);
                url = URLDecoder.decode(beginIndex, utf8);
            } catch (UnsupportedEncodingException ignored) {
                MessageUtil.debug("Cancelled" + packetName + " for " + playerName + " (could not decode URL)");
                event.setCancelled(true);
                return;
            }

            if (scheme.equals("level") && (url.contains("..") || !url.endsWith("/resources.zip"))) {
                MessageUtil.debug("Cancelled" + packetName + " for " + playerName + " (contained invalid level URL)");
                event.setCancelled(true);
                return;
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles WINDOW_CONFIRMATION packets.
        if (packetType == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            @NotNull WrapperPlayServerWindowConfirmation packet = new WrapperPlayServerWindowConfirmation(event);
            short actionNumber = packet.getActionId();

            playerData.getTransactionSentMap().put(actionNumber, System.currentTimeMillis());
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles OPEN_WINDOW packets.
        if (packetType == PacketType.Play.Server.OPEN_WINDOW) {
            playerData.setInventoryOpen(true);
            playerData.setBlocking(false);
            playerData.setShootingBow(false);
            playerData.setEating(false);
            playerData.setDrinking(false);
            playerData.setTimestamp(ActionType.INVENTORY_OPEN);
            handlePacketChecks(playerData, event);
            return;
        }

        // Handles ENTITY_TELEPORT packets.
        if (packetType == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            @NotNull WrapperPlayServerPlayerPositionAndLook teleportPacket = new WrapperPlayServerPlayerPositionAndLook(event);
            playerData.setLastTeleportPacket(teleportPacket);
            playerData.setTimestamp(ActionType.TELEPORT);

            Vector3d position = teleportPacket.getPosition();
            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();
            float yaw = teleportPacket.getYaw();
            float pitch = teleportPacket.getPitch();
            @NotNull CustomLocation loc = new CustomLocation(x, y, z, yaw, pitch);
            Queue<CustomLocation> teleports = playerData.getTeleports();

            // These packets can be received outside the tick start and end interval
            if (playerData.getPingTaskScheduler().isStarted()) {
                playerData.getPingTaskScheduler().scheduleTask(PingTask.start(() -> teleports.add(loc)));
            } else {
                teleports.add(loc);
            }

            handlePacketChecks(playerData, event);
            return;
        }

        // Handles packet checks for all other packets.
        handlePacketChecks(playerData, event);
    }

    /**
     * Handle the checks for the given packet event.
     *
     * @param playerData The player data.
     * @param event      The packet event.
     */
    private static void handlePacketChecks(@NotNull PlayerData playerData,
                                           @NotNull ProtocolPacketEvent event) {
        List<Check> checks = playerData.getChecks();

        // Create a copy of the checks list to avoid ConcurrentModificationException
        if (checks != null) {
            @NotNull Iterable<Check> checksCopy = new ArrayList<>(checks);

            for (@NotNull Check check : checksCopy) {
                if (check.getCheckInfo().enabled() && event instanceof PacketPlaySendEvent) {
                    check.handle((PacketPlaySendEvent) event);
                }
            }
        }
    }
}
