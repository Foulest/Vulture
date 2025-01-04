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
package net.foulest.packetevents.utils.server;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Server Version.
 * This is a nice wrapper over minecraft's protocol versions.
 * You won't have to memorize the protocol version, just memorize the server version you see in the launcher.
 *
 * @author retrooper
 * @see <a href="https://wiki.vg/Protocol_version_numbers">https://wiki.vg/Protocol_version_numbers</a>
 * @since 1.6.9
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerVersion {

    private static final String NMS_VERSION_SUFFIX = Bukkit.getServer().getClass().getPackage().getName()
            .replace(".", ",").split(",")[3];

    @Contract(pure = true)
    public static @NotNull String getNMSDirectory() {
        return "net.minecraft.server." + NMS_VERSION_SUFFIX;
    }

    @Contract(pure = true)
    public static @NotNull String getOBCDirectory() {
        return "org.bukkit.craftbukkit." + (NMS_VERSION_SUFFIX);
    }
}
