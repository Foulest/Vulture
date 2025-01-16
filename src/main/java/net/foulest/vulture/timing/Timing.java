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
package net.foulest.vulture.timing;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Estimates player client time by synchronizing timestamps between server and client.
 * We assume that the server time follows the correct speed and the client should never be able to run faster.
 */
@Data
@RequiredArgsConstructor
public class Timing implements Listener {

    private final @NotNull Player player;
    private final long loginTime;

    private long clientTimePassed; // Amount of time has passed according to client
    private long pingTimePassed; // Amount of time has passed according to server synchronized with client

    public int maxCatchupTicks = 10;
    public long tickMillis = 50L;

    public void tick() {
        // Lower bound is the last synced timestamp and the maximum amount of ticks the client can lag for
        long maxCatchupTime = maxCatchupTicks * tickMillis;
        long lowerBound = Math.max(pingTimePassed - maxCatchupTime, 0L);

        long currentServerTime = getCurrentServerTime();

        // Upper bound is the current server time minus the time the player has logged in
        long upperBound = currentServerTime - loginTime;

        // Every tick increments the client time passed, but the time can not go below the lower bound
        clientTimePassed = Math.max(clientTimePassed + tickMillis, lowerBound);

        // If the client runs faster than our server time
        if (clientTimePassed > upperBound) {
            long timeOver = clientTimePassed - upperBound; // Time over the upper bound

            if (timeOver > Settings.maxTimeOverServer) {
                KickUtil.kickPlayer(player, "Modifying game speed (" + timeOver + "ms)");
            }
        }
    }

    // Server time synchronization with client
    public void ping(long time) {
        pingTimePassed = time - loginTime;
    }

    /**
     * Gets the current server time.
     *
     * @return Current server time
     */
    private static long getCurrentServerTime() {
        return System.currentTimeMillis(); // Same as current system time
    }
}
