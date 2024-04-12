package io.github.retrooper.packetevents.utils.player;

import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;

/**
 * Player hand.
 * This enum represents which hand a player used to interact with.
 *
 * @author retrooper
 * @see WrappedPacketInUseEntity#getHand()
 * @see <a href="https://wiki.vg/Protocol#Open_Book">https://wiki.vg/Protocol#Open_Book</a>
 * @since 1.7.9
 */
public enum ClientHand {
    MAIN_HAND,
    OFF_HAND
}
