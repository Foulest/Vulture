/*
 * Vulture - a server protection plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
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
package net.foulest.vulture.check.type.clientbrand.type;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum DataType {
    BRAND,
    REGISTER_DATA_MOD,
    REGISTER_DATA_OTHER,
    CHANNEL;

    @Contract(pure = true)
    public @NotNull String getName() {
        switch (this) {
            case BRAND:
                return "Brand";
            case REGISTER_DATA_MOD:
                return "Mod Data";
            case REGISTER_DATA_OTHER:
                return "Register Data";
            case CHANNEL:
                return "Channel";
            default:
                return "Unknown";
        }
    }
}
