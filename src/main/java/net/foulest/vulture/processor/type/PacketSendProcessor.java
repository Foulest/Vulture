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
package net.foulest.vulture.processor.type;

import net.foulest.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import net.foulest.packetevents.event.impl.PacketPlaySendEvent;
import net.foulest.packetevents.packettype.PacketType;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.play.out.abilities.WrappedPacketOutAbilities;
import net.foulest.packetevents.packetwrappers.play.out.position.WrappedPacketOutPosition;
import net.foulest.packetevents.packetwrappers.play.out.resourcepacksend.WrappedPacketOutResourcePackSend;
import net.foulest.packetevents.packetwrappers.play.out.transaction.WrappedPacketOutTransaction;
import net.foulest.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.data.PlayerDataManager;
import net.foulest.vulture.ping.PingTask;
import net.foulest.vulture.processor.Processor;
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
 * Handles all outgoing packets before they are encoded.
 *
 * @author Foulest
 */
public class PacketSendProcessor extends Processor {

    /**
     * Handles outgoing packets before they are encoded.
     *
     * @param event The packet event.
     */
    @Override
    @SuppressWarnings("NestedMethodCall")
    public void onPacketPlaySend(@NotNull PacketPlaySendEvent event) {
        byte packetId = event.getPacketId();

        // Ignores invalid outgoing packets.
        if (PacketType.getPacketFromId(packetId) == null) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        NMSPacket nmsPacket = event.getNMSPacket();

        switch (packetId) {
            case PacketType.Play.Server.ABILITIES:
                WrappedPacketOutAbilities abilities = new WrappedPacketOutAbilities(nmsPacket);
                boolean abilitiesFlying = abilities.isFlying();
                boolean abilitiesFlightAllowed = abilities.isFlightAllowed();
                boolean abilitiesInstantBuild = abilities.canBuildInstantly();
                boolean abilitiesVulnerable = abilities.isVulnerable();

                playerData.setFlying(abilitiesFlying);
                playerData.setFlightAllowed(abilitiesFlightAllowed);
                playerData.setInstantBuild(abilitiesInstantBuild);
                playerData.setVulnerable(abilitiesVulnerable);

                if (abilitiesFlying) {
                    playerData.setTimestamp(ActionType.START_FLYING);
                } else {
                    playerData.setTimestamp(ActionType.STOP_FLYING);
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

            case PacketType.Play.Server.CLOSE_WINDOW:
                playerData.setInventoryOpen(false);
                break;

            case PacketType.Play.Server.RESOURCE_PACK_SEND:
                WrappedPacketOutResourcePackSend resourcePackSend = new WrappedPacketOutResourcePackSend(nmsPacket);
                String url = resourcePackSend.getUrl();
                String scheme = URI.create(url).getScheme();

                if (scheme == null) {
                    event.setCancelled(true);
                    MessageUtil.debug("ResourcePackSend packet cancelled; contained null URI scheme");
                    break;
                }

                if (!scheme.equals("https") && !scheme.equals("http") && !scheme.equals("level")) {
                    event.setCancelled(true);
                    MessageUtil.debug("ResourcePackSend packet cancelled; contained invalid URI scheme");
                    break;
                }

                try {
                    String utf8 = StandardCharsets.UTF_8.toString();
                    int levelLength = "level://".length();
                    String beginIndex = url.substring(levelLength);
                    url = URLDecoder.decode(beginIndex, utf8);
                } catch (UnsupportedEncodingException ignored) {
                    event.setCancelled(true);
                    MessageUtil.debug("ResourcePackSend packet cancelled; could not decode URL");
                    break;
                }

                if (scheme.equals("level") && (url.contains("..") || !url.endsWith("/resources.zip"))) {
                    event.setCancelled(true);
                    MessageUtil.debug("ResourcePackSend packet cancelled; contained invalid level URL");
                    break;
                }
                break;

            case PacketType.Play.Server.TRANSACTION:
                WrappedPacketOutTransaction transaction = new WrappedPacketOutTransaction(nmsPacket);
                short actionNumber = transaction.getActionNumber();
                playerData.getTransactionSentMap().put(actionNumber, System.currentTimeMillis());
                break;

            case PacketType.Play.Server.OPEN_WINDOW:
                playerData.setInventoryOpen(true);
                playerData.setBlocking(false);
                playerData.setShootingBow(false);
                playerData.setEating(false);
                playerData.setDrinking(false);
                playerData.setTimestamp(ActionType.INVENTORY_OPEN);
                break;

            case PacketType.Play.Server.POSITION:
                WrappedPacketOutPosition positionPacket = new WrappedPacketOutPosition(nmsPacket);
                playerData.setLastTeleportPacket(positionPacket);
                playerData.setTimestamp(ActionType.TELEPORT);

                Vector3d position = positionPacket.getPosition();
                double x = position.getX();
                double y = position.getY();
                double z = position.getZ();
                float yaw = positionPacket.getYaw();
                float pitch = positionPacket.getPitch();
                CustomLocation loc = new CustomLocation(x, y, z, yaw, pitch);
                Queue<CustomLocation> teleports = playerData.getTeleports();

                // These packets can be received outside the tick start and end interval
                if (playerData.getPingTaskScheduler().isStarted()) {
                    playerData.getPingTaskScheduler().scheduleTask(PingTask.start(() -> teleports.add(loc)));
                } else {
                    teleports.add(loc);
                }
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
    private static void handlePacketChecks(@NotNull PlayerData playerData,
                                           @NotNull CancellableNMSPacketEvent event) {
        long timestamp = System.currentTimeMillis();
        NMSPacket nmsPacket = event.getNMSPacket();
        List<Check> checks = playerData.getChecks();

        // Create a copy of the checks list to avoid ConcurrentModificationException
        if (checks != null) {
            Iterable<Check> checksCopy = new ArrayList<>(checks);

            for (Check check : checksCopy) {
                if (check.getCheckInfo().enabled() && check.getCheckInfo().acceptsServerPackets()) {
                    byte packetId = event.getPacketId();
                    Object rawNMSPacket = nmsPacket.getRawNMSPacket();

                    check.handle(event, packetId, nmsPacket, rawNMSPacket, timestamp);
                }
            }
        }
    }
}
