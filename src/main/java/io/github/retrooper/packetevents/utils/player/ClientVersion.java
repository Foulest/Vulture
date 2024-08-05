/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.retrooper.packetevents.utils.player;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Client Version.
 * This is a nice tool for minecraft's client protocol versions.
 * You won't have to memorize the protocol version, just memorize the client version
 * as the version you see in the minecraft launcher.
 * Some enum constants may represent two or more versions as there have been cases where some versions have the same protocol version due to no protocol changes.
 * We added a comment over those enum constants so check it out.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol_version_numbers">https://wiki.vg/Protocol_version_numbers</a>
 * @since 1.6.9
 */
@Getter
@ToString
public enum ClientVersion {
    v_1_8(47, "1.8 - 1.8.9"),

    v_1_9(107, "1.9"),
    v_1_9_1(108, "1.9.1"),
    v_1_9_2(109, "1.9.2"),
    v_1_9_3(110, "1.9.3 - 1.9.4"),

    v_1_10(210, "1.10 - 1.10.2"),

    v_1_11(315, "1.11"),
    v_1_11_1(316, "1.11.1 - 1.11.2"),

    v_1_12(335, "1.12"),
    v_1_12_1(338, "1.12.1"),
    v_1_12_2(340, "1.12.2"),

    v_1_13(393, "1.13"),
    v_1_13_1(401, "1.13.1"),
    v_1_13_2(404, "1.13.2"),

    v_1_14(477, "1.14"),
    v_1_14_1(480, "1.14.1"),
    v_1_14_2(485, "1.14.2"),
    v_1_14_3(490, "1.14.3"),
    v_1_14_4(498, "1.14.4"),

    v_1_15(573, "1.15"),
    v_1_15_1(575, "1.15.1"),
    v_1_15_2(578, "1.15.2"),

    v_1_16(735, "1.16"),
    v_1_16_1(736, "1.16.1"),
    v_1_16_2(751, "1.16.2"),
    v_1_16_3(753, "1.16.3"),
    v_1_16_5(754, "1.16.4 - 1.16.5"),

    v_1_17(755, "1.17"),
    v_1_17_1(756, "1.17.1"),

    v_1_18(757, "1.18 - 1.18.1"),
    v_1_18_2(758, "1.18.2"),

    v_1_19(759, "1.19"),
    v_1_19_1(760, "1.19.1 - 1.19.2"),
    v_1_19_3(761, "1.19.3"),
    v_1_19_4(762, "1.19.4"),

    v_1_20(763, "1.20 - 1.20.1"),
    v_1_20_2(764, "1.20.2"),
    v_1_20_3(765, "1.20.3 - 1.20.4"),
    v_1_20_5(766, "1.20.5 - 1.20.6"),

    v_1_21(767, "1.21"),

    LOWER_THAN_SUPPORTED_VERSIONS(v_1_8.protocolVersion - 1, "Lower Than Supported"),
    HIGHER_THAN_SUPPORTED_VERSIONS(v_1_21.protocolVersion + 1, "Higher Than Supported"),

    /**
     * Pre releases just aren't supported, we would end up with so many enum constants.
     * This constant assures you they are on a pre-release.
     */
    ANY_PRE_RELEASE_VERSION(0, "Pre-Release Version"),

    TEMP_UNRESOLVED(-1, "Unresolved"),

    UNKNOWN(-1, "Unknown");

    private static final int LOWEST_SUPPORTED_PROTOCOL_VERSION = LOWER_THAN_SUPPORTED_VERSIONS.protocolVersion + 1;
    private static final int HIGHEST_SUPPORTED_PROTOCOL_VERSION = HIGHER_THAN_SUPPORTED_VERSIONS.protocolVersion - 1;

    private static final Map<Integer, ClientVersion> CLIENT_VERSION_CACHE = new IdentityHashMap<>();

    private int protocolVersion;
    private final String displayName;

    ClientVersion(int protocolVersion, String displayName) {
        this.protocolVersion = protocolVersion;
        this.displayName = displayName;
    }

    /**
     * Get a ClientVersion enum by protocol version.
     *
     * @param protocolVersion Protocol version.
     * @return ClientVersion
     */
    public static ClientVersion getClientVersion(int protocolVersion) {
        if (protocolVersion == -1) {
            return TEMP_UNRESOLVED;

        } else if (protocolVersion < LOWEST_SUPPORTED_PROTOCOL_VERSION) {
            return LOWER_THAN_SUPPORTED_VERSIONS;

        } else if (protocolVersion > HIGHEST_SUPPORTED_PROTOCOL_VERSION) {
            return HIGHER_THAN_SUPPORTED_VERSIONS;

        } else {
            ClientVersion cached = CLIENT_VERSION_CACHE.get(protocolVersion);

            if (cached == null) {
                for (ClientVersion version : values()) {
                    if (version.protocolVersion > protocolVersion) {
                        break;
                    } else if (version.protocolVersion == protocolVersion) {
                        //Cache for next time
                        CLIENT_VERSION_CACHE.put(protocolVersion, version);
                        return version;
                    }
                }

                cached = UNKNOWN;
                cached.protocolVersion = protocolVersion;
            }
            return cached;
        }
    }

    /**
     * Is this client version newer than the compared client version?
     * This method simply checks if this client version's protocol version is greater than
     * the compared client version's protocol version.
     *
     * @param target Compared client version.
     * @return Is this client version newer than the compared client version.
     */
    @Contract(pure = true)
    public boolean isNewerThan(@NotNull ClientVersion target) {
        return protocolVersion > target.protocolVersion
                && (target != TEMP_UNRESOLVED && this != TEMP_UNRESOLVED);
    }

    /**
     * Is this client version newer than or equal to the compared client version?
     * This method simply checks if this client version's protocol version is newer than or equal to
     * the compared client version's protocol version.
     *
     * @param target Compared client version.
     * @return Is this client version newer than or equal to the compared client version.
     */
    public boolean isNewerThanOrEquals(ClientVersion target) {
        return this == target || isNewerThan(target);
    }

    /**
     * Is this client version older than the compared client version?
     * This method simply checks if this client version's protocol version is less than
     * the compared client version's protocol version.
     *
     * @param target Compared client version.
     * @return Is this client version older than the compared client version.
     */
    @Contract(pure = true)
    private boolean isOlderThan(@NotNull ClientVersion target) {
        return protocolVersion < target.protocolVersion
                && (target != TEMP_UNRESOLVED && this != TEMP_UNRESOLVED);
    }

    /**
     * Is this client version older than or equal to the compared client version?
     * This method simply checks if this client version's protocol version is older than or equal to
     * the compared client version's protocol version.
     *
     * @param target Compared client version.
     * @return Is this client version older than or equal to the compared client version.
     */
    public boolean isOlderThanOrEquals(ClientVersion target) {
        return this == target || isOlderThan(target);
    }

    /**
     * Is this client version resolved?
     * This method checks if the version is not equal to TEMP_UNRESOLVED.
     *
     * @return Is resolved
     */
    public boolean isResolved() {
        return this != TEMP_UNRESOLVED;
    }
}
