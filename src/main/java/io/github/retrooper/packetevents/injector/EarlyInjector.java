package io.github.retrooper.packetevents.injector;

import org.bukkit.entity.Player;

public interface EarlyInjector extends ChannelInjector {

    void updatePlayerObject(Player player, Object channel);
}
