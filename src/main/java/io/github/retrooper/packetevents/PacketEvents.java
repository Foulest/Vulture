package io.github.retrooper.packetevents;

import io.github.retrooper.packetevents.event.impl.PostPlayerInjectEvent;
import io.github.retrooper.packetevents.event.manager.EventManager;
import io.github.retrooper.packetevents.event.manager.PEEventManager;
import io.github.retrooper.packetevents.exceptions.PacketEventsLoadFailureException;
import io.github.retrooper.packetevents.injector.GlobalChannelInjector;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.play.out.entityequipment.WrappedPacketOutEntityEquipment;
import io.github.retrooper.packetevents.processor.BukkitEventProcessorInternal;
import io.github.retrooper.packetevents.processor.PacketProcessorInternal;
import io.github.retrooper.packetevents.settings.PacketEventsSettings;
import io.github.retrooper.packetevents.utils.entityfinder.EntityFinderUtils;
import io.github.retrooper.packetevents.utils.guava.GuavaUtils;
import io.github.retrooper.packetevents.utils.netty.bytebuf.ByteBufUtil;
import io.github.retrooper.packetevents.utils.netty.bytebuf.ByteBufUtil_8;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.player.PlayerUtils;
import io.github.retrooper.packetevents.utils.server.ServerUtils;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import io.github.retrooper.packetevents.utils.version.PEVersion;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public final class PacketEvents implements Listener, EventManager {

    private static PacketEvents instance;
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
    private PacketEventsSettings settings = new PacketEventsSettings();
    private ByteBufUtil byteBufUtil;
    private volatile boolean loading;
    private volatile boolean loaded;
    private boolean initialized;
    private boolean initializing;
    private boolean terminating;
    private boolean lateBind = false;

    public static PacketEvents create(Plugin plugin) {
        if (Bukkit.isPrimaryThread()) {
            // We are on the main thread
            if (!Bukkit.getServicesManager().isProvidedFor(PacketEvents.class)) {
                // We can register in the service manager.
                instance = new PacketEvents();
                Bukkit.getServicesManager().register(PacketEvents.class, instance, plugin, ServicePriority.Normal);
                PacketEvents.plugin = plugin;
                return instance;
            } else {
                // We have already registered. Let us load what was registered.
                return instance = Bukkit.getServicesManager().load(PacketEvents.class);
            }
        } else {
            // We are off thread; we cannot use the service manager.
            if (instance == null) {
                PacketEvents.plugin = plugin;
                instance = new PacketEvents();
            }
            return instance;
        }
    }

    public static PacketEvents get() {
        return instance;
    }

    public void load() {
        if (!loaded && !loading) {
            loading = true;

            // Resolve server version and cache
            ServerVersion version = ServerVersion.getVersion();
            WrappedPacket.version = version;
            NMSUtils.version = version;
            EntityFinderUtils.version = version;
            handlerName = "pe-" + plugin.getName().toLowerCase();

            try {
                NMSUtils.load();
                PacketTypeClasses.load();
                PacketType.load();
                EntityFinderUtils.load();

                getServerUtils().entityCache = GuavaUtils.makeMap();

                if (version.isNewerThanOrEquals(ServerVersion.v_1_9)) {
                    for (WrappedPacketOutEntityEquipment.EquipmentSlot slot : WrappedPacketOutEntityEquipment.EquipmentSlot.values()) {
                        slot.id = (byte) slot.ordinal();
                    }
                } else {
                    WrappedPacketOutEntityEquipment.EquipmentSlot.MAINHAND.id = 0;
                    WrappedPacketOutEntityEquipment.EquipmentSlot.OFFHAND.id = -1; // Invalid
                    WrappedPacketOutEntityEquipment.EquipmentSlot.BOOTS.id = 1;
                    WrappedPacketOutEntityEquipment.EquipmentSlot.LEGGINGS.id = 2;
                    WrappedPacketOutEntityEquipment.EquipmentSlot.CHESTPLATE.id = 3;
                    WrappedPacketOutEntityEquipment.EquipmentSlot.HELMET.id = 4;
                }
            } catch (Exception ex) {
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

    public void loadSettings(PacketEventsSettings settings) {
        this.settings = settings;
    }

    public void init() {
        init(getSettings());
    }

    public void init(PacketEventsSettings packetEventsSettings) {
        // Load if we haven't loaded already
        load();

        if (!initialized && !initializing) {
            initializing = true;
            settings = packetEventsSettings;
            settings.lock();

            // Wait for the injector to be ready.
            while (!injectorReady.get()) {
            }

            Runnable postInjectTask = () -> {
                Bukkit.getPluginManager().registerEvents(bukkitEventProcessorInternal, plugin);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        injector.injectPlayer(player);
                        getEventManager().callEvent(new PostPlayerInjectEvent(player, false));
                    } catch (Exception ex) {
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
            getEventManager().unregisterAllListeners();
            initialized = false;
            terminating = false;
        }
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
