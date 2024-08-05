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
package net.foulest.vulture.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

import java.util.List;

@Getter
@Setter
@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TaskUtil {

    public static void runTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(Vulture.instance, runnable);
    }

    public static void runTaskAsynchronously(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(Vulture.instance, runnable);
    }

    public static void runTaskLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(Vulture.instance, runnable, delay);
    }

    public static void runTaskLaterAsynchronously(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Vulture.instance, runnable, delay);
    }

    public static void runTaskTimer(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(Vulture.instance, runnable, delay, period);
    }

    public static void runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Vulture.instance, runnable, delay, period);
    }

    public static void scheduleSyncDelayedTask(Runnable runnable) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Vulture.instance, runnable);
    }

    public static void scheduleSyncDelayedTask(Runnable runnable, long period) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Vulture.instance, runnable, period);
    }

    public static void scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Vulture.instance, runnable, delay, period);
    }

    public static void cancelAllTasks() {
        Bukkit.getScheduler().cancelAllTasks();
    }

    public static void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public static boolean isCurrentlyRunning(int taskId) {
        return Bukkit.getScheduler().isCurrentlyRunning(taskId);
    }

    public static boolean isQueued(int taskId) {
        return Bukkit.getScheduler().isQueued(taskId);
    }

    public static List<BukkitWorker> getActiveWorkers() {
        return Bukkit.getScheduler().getActiveWorkers();
    }

    public static List<BukkitTask> getPendingTasks() {
        return Bukkit.getScheduler().getPendingTasks();
    }
}
