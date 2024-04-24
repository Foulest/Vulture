package dev.thomazz.pledge;

import dev.thomazz.pledge.event.PingSendEvent;
import dev.thomazz.pledge.event.PongReceiveEvent;
import dev.thomazz.pledge.event.TickEndEvent;
import dev.thomazz.pledge.event.TickStartEvent;
import dev.thomazz.pledge.network.NetworkPongListener;
import dev.thomazz.pledge.packet.PacketProviderFactory;
import dev.thomazz.pledge.packet.PingPacketProvider;
import dev.thomazz.pledge.pinger.ClientPinger;
import dev.thomazz.pledge.pinger.ClientPingerImpl;
import dev.thomazz.pledge.pinger.frame.FrameClientPinger;
import dev.thomazz.pledge.pinger.frame.FrameClientPingerImpl;
import dev.thomazz.pledge.util.ChannelAccess;
import dev.thomazz.pledge.util.ChannelUtils;
import dev.thomazz.pledge.util.TickEndTask;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

@Getter
public class PledgeImpl implements Pledge, Listener {

    static PledgeImpl instance;

    private final Logger logger;
    private final PingPacketProvider packetProvider;

    private final BukkitTask startTask;
    private final TickEndTask endTask;

    private final List<ClientPingerImpl> clientPingers = new ArrayList<>();
    private final Map<Player, Channel> playerChannels = new HashMap<>();

    PledgeImpl(@NotNull Plugin plugin) {
        logger = plugin.getLogger();
        packetProvider = PacketProviderFactory.buildPingProvider();

        PluginManager manager = Bukkit.getPluginManager();
        BukkitScheduler scheduler = Bukkit.getScheduler();

        startTask = scheduler.runTaskTimer(plugin, () -> manager.callEvent(new TickStartEvent()), 0L, 1L);
        endTask = TickEndTask.create(() -> manager.callEvent(new TickEndEvent()));

        // Setup for all players
        Bukkit.getOnlinePlayers().forEach(this::setupPlayer);

        // Register as listener after setup
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setupPlayer(Player player) {
        Channel channel = ChannelAccess.getChannel(player);
        playerChannels.put(player, channel);

        // Inject pong listener
        channel.pipeline().addBefore(
                "packet_handler",
                "vulture_pledge_packet_listener",
                new NetworkPongListener(this, player)
        );

        // Register to client pingers
        clientPingers.forEach(pinger -> pinger.registerPlayer(player));
    }

    private void teardownPlayer(Player player) {
        playerChannels.remove(player);

        // Unregister from client pingers
        clientPingers.forEach(pinger -> pinger.unregisterPlayer(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerLogin(@NotNull PlayerLoginEvent event) {
        setupPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        teardownPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onTickStart(TickStartEvent ignored) {
        clientPingers.forEach(ClientPingerImpl::tickStart);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onTickStart(TickEndEvent ignored) {
        clientPingers.forEach(ClientPingerImpl::tickEnd);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPongReceive(@NotNull PongReceiveEvent event) {
        Player player = event.getPlayer();
        int id = event.getId();

        clientPingers.stream()
                .filter(pinger -> pinger.isInRange(id))
                .forEach(
                        pinger -> pinger.getPingData(player)
                                .flatMap(data -> data.confirm(id))
                                .ifPresent(pong -> pinger.onReceive(player, pong))
                );
    }

    @Override
    public void sendPing(@NotNull Player player, int id) {
        // Keep within ranges
        int max = Math.max(packetProvider.getUpperBound(), packetProvider.getLowerBound());
        int min = Math.min(packetProvider.getUpperBound(), packetProvider.getLowerBound());
        int pingId = Math.max(Math.min(id, max), min);

        // Run on channel event loop
        getChannel(player).ifPresent(channel ->
                ChannelUtils.runInEventLoop(channel, () ->
                        sendPingRaw(player, channel, pingId)
                )
        );
    }

    public void sendPingRaw(Player player, Channel channel, int pingId) {
        try {
            Object packet = packetProvider.buildPacket(pingId);
            Bukkit.getPluginManager().callEvent(new PingSendEvent(player, pingId));
            channel.writeAndFlush(packet);
        } catch (Exception ex) {
            logger.severe(String.format("Failed to send ping! Player:%s Id:%o", player.getName(), pingId));
            ex.printStackTrace();
        }
    }

    @Override
    public Optional<Channel> getChannel(@NotNull Player player) {
        return Optional.ofNullable(playerChannels.get(player));
    }

    @Override
    public ClientPinger createPinger(int startId, int endId) {
        ClientPingerImpl pinger = new ClientPingerImpl(this, startId, endId);
        clientPingers.add(pinger);
        return pinger;
    }

    @Override
    public FrameClientPinger createFramePinger(int startId, int endId) {
        FrameClientPingerImpl pinger = new FrameClientPingerImpl(this, startId, endId);
        clientPingers.add(pinger);
        return pinger;
    }

    @Override
    public void destroy() {
        if (!equals(PledgeImpl.instance)) {
            throw new IllegalStateException("API object not the same as current instance!");
        }

        // Teardown for all players
        Bukkit.getOnlinePlayers().forEach(this::teardownPlayer);

        HandlerList.unregisterAll(this);
        startTask.cancel();
        endTask.cancel();

        PledgeImpl.instance = null;
    }
}
