package io.github.retrooper.packetevents.utils.gameprofile;

import lombok.Getter;

import java.util.UUID;

/**
 * Wrapper for the Player Game Profile.
 *
 * @author retrooper
 * @since 1.7
 */
@Getter
public class WrappedGameProfile {

    private final UUID id;
    private final String name;

    public WrappedGameProfile(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean isComplete() {
        return id != null && !isBlank(name);
    }

    private boolean isBlank(CharSequence cs) {
        int strLen;

        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
