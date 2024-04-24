package dev.thomazz.pledge.pinger;

import dev.thomazz.pledge.PledgeImpl;
import dev.thomazz.pledge.network.NetworkPacketConsolidator;
import dev.thomazz.pledge.packet.PingPacketProvider;
import dev.thomazz.pledge.pinger.data.Ping;
import dev.thomazz.pledge.pinger.data.PingData;
import dev.thomazz.pledge.pinger.data.PingOrder;
import dev.thomazz.pledge.util.ChannelUtils;
import io.netty.channel.Channel;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;

@Getter
public class ClientPingerImpl implements ClientPinger {

    protected final Map<Player, PingData> pingDataMap = new LinkedHashMap<>();
    protected final List<ClientPingerListener> pingListeners = new ArrayList<>();

    protected final PledgeImpl api;
    protected final int startId;
    protected final int endId;

    protected Predicate<Player> playerFilter = player -> true;

    public ClientPingerImpl(PledgeImpl api, int startId, int endId) {
        this.api = api;

        PingPacketProvider provider = api.getPacketProvider();
        int upperBound = provider.getUpperBound();
        int lowerBound = provider.getLowerBound();

        this.startId = Math.max(Math.min(upperBound, startId), lowerBound);
        this.endId = Math.max(Math.min(upperBound, endId), lowerBound);

        if (this.startId != startId) {
            this.api.getLogger().warning("Changed start ID to fit bounds: " + startId + " -> " + this.startId);
        }

        if (this.endId != endId) {
            this.api.getLogger().warning("Changed end ID to fit bounds: " + endId + " -> " + this.endId);
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
    protected void ping(Player player, Channel channel, Ping ping) {
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

    public Optional<PingData> getPingData(Player player) {
        return Optional.of(pingDataMap.get(player));
    }

    protected void onSend(Player player, Ping ping) {
        switch (ping.getOrder()) {
            case TICK_START:
                onSendStart(player, ping.getId());
                break;
            case TICK_END:
                onSendEnd(player, ping.getId());
                break;
        }
    }

    public void onReceive(Player player, Ping ping) {
        switch (ping.getOrder()) {
            case TICK_START:
                onReceiveStart(player, ping.getId());
                break;
            case TICK_END:
                onReceiveEnd(player, ping.getId());
                break;
        }
    }

    protected void onSendStart(Player player, int id) {
        pingListeners.forEach(listener -> listener.onPingSendStart(player, id));
    }

    protected void onSendEnd(Player player, int id) {
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
