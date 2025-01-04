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
package net.foulest.vulture.check;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;

/**
 * Data class for violations.
 *
 * @author Foulest
 */
@Data
@AllArgsConstructor
public class Violation {

    // Check Data
    private final CheckInfoData checkInfo;
    private final String[] data;
    private final int violations;

    // Player Data
    private final Location location;
    private final int ping;

    // Server Data
    private final double tps;

    // Timestamp
    private final long timestamp;
}
