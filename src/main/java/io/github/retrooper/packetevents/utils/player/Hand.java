package io.github.retrooper.packetevents.utils.player;

/**
 * The {@code Hand} enum represents what hand was used in an interaction.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol#Open_Book">https://wiki.vg/Protocol#Open_Book</a>
 * @since 1.8
 */
public enum Hand {
    /**
     * The right hand in vanilla minecraft.
     * Some clients allow you to render the main hand as the left hand.
     */
    MAIN_HAND,

    /**
     * The left hand in vanilla minecraft.
     * This hand does not exist on 1.7.10 and 1.8.x's protocol.
     * It will always be the {@link Hand#MAIN_HAND} on those protocols.
     */
    OFF_HAND
}
