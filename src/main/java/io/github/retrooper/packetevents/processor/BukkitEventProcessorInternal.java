package io.github.retrooper.packetevents.processor;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.impl.PostPlayerInjectEvent;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import io.github.retrooper.packetevents.utils.versionlookup.VersionLookupUtils;
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
    public void onLogin(@NotNull PlayerLoginEvent event) {
        Player player = event.getPlayer();
        int protocolVersion = VersionLookupUtils.getProtocolVersion(player);
        ClientVersion version = ClientVersion.getClientVersion(protocolVersion);

        if (version == ClientVersion.LOWER_THAN_SUPPORTED_VERSIONS) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "&cYour client version is not supported by this server.");
            return;
        }

        PacketEvents.get().getInjector().injectPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InetSocketAddress address = player.getAddress();
        boolean shouldInject = !(PacketEvents.get().getInjector().hasInjected(event.getPlayer()));

        // Inject now if we are using the compatibility-injector or inject if the early injector failed to inject them.
        if (shouldInject) {
            PacketEvents.get().getInjector().injectPlayer(player);
        }

        boolean dependencyAvailable = VersionLookupUtils.isDependencyAvailable();
        PacketEvents.get().getPlayerUtils().loginTime.put(player.getUniqueId(), System.currentTimeMillis());

        // A supported dependency is available, we need to first ask the dependency for the client version.
        if (dependencyAvailable) {
            // We are resolving version one tick later for extra safety.
            // Some dependencies throw exceptions if we try too early.
            Bukkit.getScheduler().runTaskLaterAsynchronously(PacketEvents.get().getPlugin(), () -> {
                try {
                    int protocolVersion = VersionLookupUtils.getProtocolVersion(player);
                    ClientVersion version = ClientVersion.getClientVersion(protocolVersion);
                    PacketEvents.get().getPlayerUtils().clientVersionsMap.put(address, version);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                PacketEvents.get().getEventManager().callEvent(new PostPlayerInjectEvent(player, true));
            }, 1L);
        } else {
            // Dependency isn't available, we can already call the post player inject event.
            PacketEvents.get().getEventManager().callEvent(new PostPlayerInjectEvent(event.getPlayer(), false));
        }

        PacketEvents.get().getServerUtils().entityCache.putIfAbsent(event.getPlayer().getEntityId(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        InetSocketAddress address = player.getAddress();

        // Cleanup user data
        PacketEvents.get().getPlayerUtils().loginTime.remove(uuid);
        PacketEvents.get().getPlayerUtils().playerPingMap.remove(uuid);
        PacketEvents.get().getPlayerUtils().playerSmoothedPingMap.remove(uuid);
        PacketEvents.get().getPlayerUtils().clientVersionsMap.remove(address);
        PacketEvents.get().getPlayerUtils().tempClientVersionMap.remove(address);
        PacketEvents.get().getPlayerUtils().keepAliveMap.remove(uuid);
        PacketEvents.get().getPlayerUtils().channels.remove(player.getName());
        PacketEvents.get().getServerUtils().entityCache.remove(event.getPlayer().getEntityId());
    }

    @EventHandler
    public void onEntitySpawn(@NotNull EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        PacketEvents.get().getServerUtils().entityCache.putIfAbsent(entity.getEntityId(), entity);
    }

    @EventHandler
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        PacketEvents.get().getServerUtils().entityCache.remove(event.getEntity().getEntityId());
    }
}
