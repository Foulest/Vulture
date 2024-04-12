package io.github.retrooper.packetevents.event.eventtypes;

import io.github.retrooper.packetevents.event.impl.PacketPlayReceiveEvent;
import org.bukkit.entity.Player;

/**
 * Every event that has an associated player is recommended to implement this interface.
 *
 * @author retrooper
 * @see PacketPlayReceiveEvent
 * @since 1.6.9
 */
public interface PlayerEvent {

    /**
     * Associated player.
     *
     * @return Player.
     */
    Player getPlayer();
}
