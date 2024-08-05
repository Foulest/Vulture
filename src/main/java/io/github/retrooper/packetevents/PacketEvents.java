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
package io.github.retrooper.packetevents;

import io.github.retrooper.packetevents.event.impl.PostPlayerInjectEvent;
import io.github.retrooper.packetevents.event.manager.EventManager;
import io.github.retrooper.packetevents.event.manager.PEEventManager;
import io.github.retrooper.packetevents.exceptions.PacketEventsLoadFailureException;
import io.github.retrooper.packetevents.injector.GlobalChannelInjector;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.play.out.entityequipment.WrappedPacketOutEntityEquipment;
import io.github.retrooper.packetevents.processor.BukkitEventProcessorInternal;
import io.github.retrooper.packetevents.processor.PacketProcessorInternal;
import io.github.retrooper.packetevents.utils.entityfinder.EntityFinderUtils;
import io.github.retrooper.packetevents.utils.guava.GuavaUtils;
import io.github.retrooper.packetevents.utils.netty.bytebuf.ByteBufUtil;
import io.github.retrooper.packetevents.utils.netty.bytebuf.ByteBufUtil_8;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.player.PlayerUtils;
import io.github.retrooper.packetevents.utils.server.ServerUtils;
import io.github.retrooper.packetevents.utils.version.PEVersion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@ToString
@NoArgsConstructor
public final class PacketEvents implements Listener, EventManager {

    @Getter
    private static PacketEvents instance;
    @Getter
    private static Plugin plugin;
    private final PEVersion version = new PEVersion(1, 8, 4);
    private final EventManager eventManager = new PEEventManager();
    private final PlayerUtils playerUtils = new PlayerUtils();
    private final ServerUtils serverUtils = new ServerUtils();
    private final PacketProcessorInternal internalPacketProcessor = new PacketProcessorInternal();
    private final BukkitEventProcessorInternal bukkitEventProcessorInternal = new BukkitEventProcessorInternal();
    private final GlobalChannelInjector injector = new GlobalChannelInjector();
    private final AtomicBoolean injectorReady = new AtomicBoolean();
    private String handlerName;
    private ByteBufUtil byteBufUtil;
    private volatile boolean loading;
    private volatile boolean loaded;
    private boolean initialized;
    private boolean initializing;
    private boolean terminating;
    private boolean lateBind;

    public static PacketEvents create(Plugin plugin) {
        if (Bukkit.isPrimaryThread()) {
            // We are on the main thread
            if (Bukkit.getServicesManager().isProvidedFor(PacketEvents.class)) {
                // We have already registered. Let us load what was registered.
                instance = Bukkit.getServicesManager().load(PacketEvents.class);
            } else {
                // We can register in the service manager.
                instance = new PacketEvents();
                Bukkit.getServicesManager().register(PacketEvents.class, instance, plugin, ServicePriority.Normal);
                PacketEvents.plugin = plugin;
            }
        } else {
            // We are off thread; we cannot use the service manager.
            if (instance == null) {
                PacketEvents.plugin = plugin;
                instance = new PacketEvents();
            }
        }
        return instance;
    }

    public void load() {
        if (!loaded && !loading) {
            loading = true;
            handlerName = "pe-" + plugin.getName().toLowerCase(Locale.ROOT);

            try {
                NMSUtils.load();
                PacketTypeClasses.load();
                PacketType.load();
                EntityFinderUtils.load();

                serverUtils.entityCache = GuavaUtils.makeMap();

                WrappedPacketOutEntityEquipment.EquipmentSlot.MAINHAND.id = 0;
                WrappedPacketOutEntityEquipment.EquipmentSlot.BOOTS.id = 1;
                WrappedPacketOutEntityEquipment.EquipmentSlot.LEGGINGS.id = 2;
                WrappedPacketOutEntityEquipment.EquipmentSlot.CHESTPLATE.id = 3;
                WrappedPacketOutEntityEquipment.EquipmentSlot.HELMET.id = 4;
            } catch (RuntimeException ex) {
                loading = false;
                throw new PacketEventsLoadFailureException(ex);
            }

            byteBufUtil = new ByteBufUtil_8();

            if (!injectorReady.get()) {
                injector.load();
                lateBind = !injector.isBound();

                // If late-bind is enabled, we will inject a bit later.
                if (!lateBind) {
                    injector.inject();
                }

                injectorReady.set(true);
            }

            loaded = true;
            loading = false;
        }
    }

    public void init() {
        // Load if we haven't loaded already
        load();

        if (!initialized && !initializing) {
            initializing = true;

            while (!injectorReady.get()) {
                // Wait for the injector to be ready.
            }

            Runnable postInjectTask = () -> {
                Bukkit.getPluginManager().registerEvents(bukkitEventProcessorInternal, plugin);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        injector.injectPlayer(player);
                        eventManager.callEvent(new PostPlayerInjectEvent(player, false));
                    } catch (RuntimeException ex) {
                        player.kickPlayer("Failed to inject... Please rejoin!");
                    }
                }
            };

            if (lateBind) {
                // If late-bind is enabled, we still need to inject (after all plugins enabled).
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, injector::inject);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, postInjectTask);
            } else {
                postInjectTask.run();
            }

            initialized = true;
            initializing = false;
        }
    }

    public void terminate() {
        if (initialized && !terminating) {
            // Eject all players
            for (Player player : Bukkit.getOnlinePlayers()) {
                injector.ejectPlayer(player);
            }

            // Eject the injector if needed
            injector.eject();

            // Unregister all our listeners
            eventManager.unregisterAllListeners();
            initialized = false;
            terminating = false;
        }
    }
}
