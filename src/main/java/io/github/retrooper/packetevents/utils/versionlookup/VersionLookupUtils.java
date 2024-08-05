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
package io.github.retrooper.packetevents.utils.versionlookup;

import io.github.retrooper.packetevents.utils.versionlookup.viaversion.ViaVersionLookupUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VersionLookupUtils {

    public static boolean isDependencyAvailable() {
        return ViaVersionLookupUtils.isAvailable();
    }

    public static int getProtocolVersion(Player player) {
        if (ViaVersionLookupUtils.isAvailable()) {
            return ViaVersionLookupUtils.getProtocolVersion(player);
        }
        return -1;
    }
}
