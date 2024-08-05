/*
 * This file is part of Pledge - https://github.com/ThomasOM/Pledge
 * Copyright (C) 2021 Thomazz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
import lombok.ToString;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

@Getter
@ToString
public class PledgeImpl implements Pledge, Listener {

    static @Nullable PledgeImpl instance;

    @SuppressWarnings("NonConstantLogger")
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

        // Remove pre-existing pong listener
        if (channel.pipeline().get("vulture_pledge_packet_listener") != null) {
            channel.pipeline().remove("vulture_pledge_packet_listener");
        }

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
        if (!equals(instance)) {
            throw new IllegalStateException("API object not the same as current instance!");
        }

        // Teardown for all players
        Bukkit.getOnlinePlayers().forEach(this::teardownPlayer);

        HandlerList.unregisterAll(this);
        startTask.cancel();
        endTask.cancel();

        instance = null;
    }
}
