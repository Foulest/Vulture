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
package net.foulest.pledge.pinger;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.ToString;
import net.foulest.pledge.PledgeImpl;
import net.foulest.pledge.network.NetworkPacketConsolidator;
import net.foulest.pledge.packet.PingPacketProvider;
import net.foulest.pledge.pinger.data.Ping;
import net.foulest.pledge.pinger.data.PingData;
import net.foulest.pledge.pinger.data.PingOrder;
import net.foulest.pledge.util.ChannelUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

@Getter
@ToString
public class ClientPingerImpl implements ClientPinger {

    protected final Map<Player, PingData> pingDataMap = new LinkedHashMap<>();
    protected final List<ClientPingerListener> pingListeners = new ArrayList<>();

    protected final PledgeImpl api;
    protected final int startId;
    protected final int endId;

    protected Predicate<Player> playerFilter = player -> true;

    public ClientPingerImpl(@NotNull PledgeImpl api, int startId, int endId) {
        this.api = api;

        PingPacketProvider provider = api.getPacketProvider();
        int upperBound = provider.getUpperBound();
        int lowerBound = provider.getLowerBound();

        this.startId = Math.max(Math.min(upperBound, startId), lowerBound);
        this.endId = Math.max(Math.min(upperBound, endId), lowerBound);

        if (this.startId != startId) {
            this.api.getLogger().warning(String.format("Changed start ID to fit bounds: %d -> %d", startId, this.startId));
        }

        if (this.endId != endId) {
            this.api.getLogger().warning(String.format("Changed end ID to fit bounds: %d -> %d", endId, this.endId));
        }
    }

    @Override
    public int startId() {
        return startId;
    }

    @Override
    public int endId() {
        return endId;
    }

    @Override
    public void filter(Predicate<Player> condition) {
        playerFilter = condition;
    }

    @Override
    public void attach(ClientPingerListener listener) {
        pingListeners.add(listener);
    }

    public void registerPlayer(Player player) {
        if (playerFilter.test(player)) {
            injectPlayer(player);
            pingDataMap.put(player, new PingData(player, this));
        }
    }

    public void unregisterPlayer(Player player) {
        pingDataMap.remove(player);
        ejectPlayer(player);
    }

    protected void injectPlayer(Player player) {
        api.getChannel(player).ifPresent(channel ->
                ChannelUtils.runInEventLoop(channel,
                        () -> channel.pipeline().addLast("vulture_pledge_tick_consolidator", new NetworkPacketConsolidator())
                )
        );
    }

    protected void ejectPlayer(Player player) {
        api.getChannel(player).ifPresent(channel ->
                ChannelUtils.runInEventLoop(channel,
                        () -> channel.pipeline().remove(NetworkPacketConsolidator.class)
                )
        );
    }

    // Note: Should run in channel event loop
    protected void ping(Player player, @NotNull Channel channel, Ping ping) {
        if (!channel.eventLoop().inEventLoop()) {
            throw new IllegalStateException("Tried to run ping outside event loop!");
        }

        api.sendPingRaw(player, channel, ping.getId());
        getPingData(player).ifPresent(data -> data.offer(ping));
        onSend(player, ping);
    }

    public boolean isInRange(int id) {
        return id >= Math.min(startId, endId) && id <= Math.max(startId, endId);
    }

    // Fixes an NPE in the original code
    public Optional<PingData> getPingData(Player player) {
        return Optional.ofNullable(pingDataMap.get(player));
    }

    private void onSend(Player player, @NotNull Ping ping) {
        switch (ping.getOrder()) {
            case TICK_START:
                onSendStart(player, ping.getId());
                break;
            case TICK_END:
                onSendEnd(player, ping.getId());
                break;
            default:
                break;
        }
    }

    public void onReceive(Player player, @NotNull Ping ping) {
        switch (ping.getOrder()) {
            case TICK_START:
                onReceiveStart(player, ping.getId());
                break;
            case TICK_END:
                onReceiveEnd(player, ping.getId());
                break;
            default:
                break;
        }
    }

    private void onSendStart(Player player, int id) {
        pingListeners.forEach(listener -> listener.onPingSendStart(player, id));
    }

    private void onSendEnd(Player player, int id) {
        pingListeners.forEach(listener -> listener.onPingSendEnd(player, id));
    }

    protected void onReceiveStart(Player player, int id) {
        pingListeners.forEach(listener -> listener.onPongReceiveStart(player, id));
    }

    protected void onReceiveEnd(Player player, int id) {
        pingListeners.forEach(listener -> listener.onPongReceiveEnd(player, id));
    }

    public void tickStart() {
        pingDataMap.forEach((player, data) ->
                api.getChannel(player).ifPresent(channel ->
                        ChannelUtils.runInEventLoop(channel, () -> {
                            NetworkPacketConsolidator consolidator = channel.pipeline().get(NetworkPacketConsolidator.class);

                            if (consolidator != null) {
                                consolidator.open();
                                ping(player, channel, new Ping(PingOrder.TICK_START, data.pullId()));
                                consolidator.drain(channel.pipeline().lastContext());
                            }
                        })
                )
        );
    }

    public void tickEnd() {
        pingDataMap.forEach((player, data) ->
                api.getChannel(player).ifPresent(channel ->
                        ChannelUtils.runInEventLoop(channel, () -> {
                            NetworkPacketConsolidator consolidator = channel.pipeline().get(NetworkPacketConsolidator.class);

                            if (consolidator != null) {
                                ping(player, channel, new Ping(PingOrder.TICK_END, data.pullId()));
                                consolidator.close();
                            }
                        })
                )
        );
    }
}
