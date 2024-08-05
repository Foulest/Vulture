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
package net.foulest.vulture.ping;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Task interface for {@link PingTaskScheduler}
 */
public interface PingTask {

    default void onStart() {
    } // Executes on first pong received

    default void onEnd() {
    } // Executes on second pong received

    @Contract(value = "_ -> new", pure = true)
    static @NotNull PingTask start(Runnable runnable) {
        return of(runnable, () -> {
        });
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull PingTask end(Runnable runnable) {
        return of(() -> {
        }, runnable);
    }

    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull PingTask of(Runnable r1, Runnable r2) {
        return new PingTask() {
            @Override
            public void onStart() {
                r1.run();
            }

            @Override
            public void onEnd() {
                r2.run();
            }
        };
    }
}
