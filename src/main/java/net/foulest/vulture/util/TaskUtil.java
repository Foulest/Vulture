package net.foulest.vulture.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

import java.util.List;

@Getter
@Setter
@SuppressWarnings("unused")
public class TaskUtil {

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
