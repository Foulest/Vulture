package io.github.retrooper.packetevents.utils.player;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The {@code Direction} enum contains constants for the different valid faces in the minecraft protocol.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol#Player_Digging">https://wiki.vg/Protocol#Player_Digging</a>
 * @since 1.7.8
 */
@Getter
@AllArgsConstructor
public enum Direction {
    /**
     * -Y offset
     */
    DOWN,

    /**
     * +Y offset
     */
    UP,

    /**
     * -Z offset
     */
    NORTH,

    /**
     * +Z offset
     */
    SOUTH,

    /**
     * -X offset
     */
    WEST,

    /**
     * +X offset
     */
    EAST,

    /**
     * Face is set to 255
     */
    OTHER((short) 255),

    /**
     * Should not happen.... Invalid value?
     */
    INVALID;

    final short faceValue;

    Direction() {
        faceValue = (short) ordinal();
    }

    public static Direction getDirection(int face) {
        if (face == 255) {
            return OTHER;
        } else if (face < 0 || face > 5) {
            return INVALID;
        }
        return values()[face];
    }
}
