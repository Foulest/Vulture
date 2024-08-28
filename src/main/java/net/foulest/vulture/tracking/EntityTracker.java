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
package net.foulest.vulture.tracking;

import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages entity movement tracking from packet data received by the client for a single player.
 */
@Setter
@ToString
public class EntityTracker {

    private final Map<Integer, EntityTrackerEntry> entries = new LinkedHashMap<>();

    // Called on client tick to interpolate entities from the current client position to the next one sent by the server
    public void interpolate() {
        entries.values().forEach(EntityTrackerEntry::interpolate);
    }

    // Only when players are added to the client world
    public void addEntity(int id, double x, double y, double z) {
        EntityTrackerEntry entry = new EntityTrackerEntry(x, y, z);
        entries.put(id, entry);
    }

    // When an entity gets destroyed
    public void removeEntity(int id) {
        entries.remove(id);
    }

    // Relative entity moves
    public void moveEntity(int id, double dx, double dy, double dz) {
        getEntry(id).ifPresent(entry -> entry.move(dx, dy, dz));
    }

    // Entity teleports
    public void teleportEntity(int id, double x, double y, double z) {
        getEntry(id).ifPresent(entry -> entry.teleport(x, y, z));
    }

    // Called when second pong is received for move or teleport to confirm the client has received the update
    public void markCertain(int id) {
        getEntry(id).ifPresent(EntityTrackerEntry::markCertain);
    }

    public Optional<EntityTrackerEntry> getEntry(int entityId) {
        return Optional.ofNullable(entries.get(entityId));
    }
}
