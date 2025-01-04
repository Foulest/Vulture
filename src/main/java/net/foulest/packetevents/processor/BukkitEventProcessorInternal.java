/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
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
package net.foulest.packetevents.processor;

import net.foulest.packetevents.PacketEvents;
import net.foulest.packetevents.event.impl.PostPlayerInjectEvent;
import net.foulest.packetevents.utils.player.ClientVersion;
import net.foulest.packetevents.utils.versionlookup.VersionLookupUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.UUID;

public class BukkitEventProcessorInternal implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public static void onLogin(@NotNull PlayerLoginEvent event) {
        Player player = event.getPlayer();
        int protocolVersion = VersionLookupUtils.getProtocolVersion(player);
        ClientVersion version = ClientVersion.getClientVersion(protocolVersion);

        if (version == ClientVersion.LOWER_THAN_SUPPORTED_VERSIONS) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "&cYour client version is not supported by this server.");
            return;
        }

        PacketEvents.getInstance().getInjector().injectPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public static void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InetSocketAddress address = player.getAddress();
        boolean shouldInject = !(PacketEvents.getInstance().getInjector().hasInjected(event.getPlayer()));

        // Inject now if we are using the compatibility-injector or inject if the early injector failed to inject them.
        if (shouldInject) {
            PacketEvents.getInstance().getInjector().injectPlayer(player);
        }

        boolean dependencyAvailable = VersionLookupUtils.isDependencyAvailable();
        PacketEvents.getInstance().getPlayerUtils().loginTime.put(player.getUniqueId(), System.currentTimeMillis());

        // A supported dependency is available, we need to first ask the dependency for the client version.
        if (dependencyAvailable) {
            // We are resolving version one tick later for extra safety.
            // Some dependencies throw exceptions if we try too early.
            Bukkit.getScheduler().runTaskLaterAsynchronously(PacketEvents.getPlugin(), () -> {
                try {
                    int protocolVersion = VersionLookupUtils.getProtocolVersion(player);
                    ClientVersion version = ClientVersion.getClientVersion(protocolVersion);
                    PacketEvents.getInstance().getPlayerUtils().clientVersionsMap.put(address, version);
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }

                PacketEvents.getInstance().getEventManager().callEvent(new PostPlayerInjectEvent(player, true));
            }, 1L);
        } else {
            // Dependency isn't available, we can already call the post player inject event.
            PacketEvents.getInstance().getEventManager().callEvent(new PostPlayerInjectEvent(event.getPlayer(), false));
        }

        PacketEvents.getInstance().getServerUtils().entityCache.putIfAbsent(event.getPlayer().getEntityId(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        InetSocketAddress address = player.getAddress();

        // Cleanup user data
        PacketEvents.getInstance().getPlayerUtils().loginTime.remove(uuid);
        PacketEvents.getInstance().getPlayerUtils().playerPingMap.remove(uuid);
        PacketEvents.getInstance().getPlayerUtils().playerSmoothedPingMap.remove(uuid);
        PacketEvents.getInstance().getPlayerUtils().clientVersionsMap.remove(address);
        PacketEvents.getInstance().getPlayerUtils().tempClientVersionMap.remove(address);
        PacketEvents.getInstance().getPlayerUtils().keepAliveMap.remove(uuid);
        PacketEvents.getInstance().getPlayerUtils().channels.remove(player.getName());
        PacketEvents.getInstance().getServerUtils().entityCache.remove(event.getPlayer().getEntityId());
    }

    @EventHandler
    public static void onEntitySpawn(@NotNull EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        PacketEvents.getInstance().getServerUtils().entityCache.putIfAbsent(entity.getEntityId(), entity);
    }

    @EventHandler
    public static void onEntityDeath(@NotNull EntityDeathEvent event) {
        PacketEvents.getInstance().getServerUtils().entityCache.remove(event.getEntity().getEntityId());
    }
}
