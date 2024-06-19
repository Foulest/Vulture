/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
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
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInfoData {

    private String name;
    private CheckType type;
    private String description = "No description provided.";
    private boolean enabled = true;
    private boolean punishable = true;
    private String banCommand = "vulture kick %player% %check%";
    private int maxViolations = 10;
    private boolean experimental = false;
    private boolean acceptsServerPackets = false;

    public CheckInfoData(@NotNull CheckInfo checkInfo) {
        name = checkInfo.name();
        type = checkInfo.type();
        description = checkInfo.description();
        enabled = checkInfo.enabled();
        punishable = checkInfo.punishable();
        banCommand = checkInfo.banCommand();
        maxViolations = checkInfo.maxViolations();
        experimental = checkInfo.experimental();
        acceptsServerPackets = checkInfo.acceptsServerPackets();
    }
}
