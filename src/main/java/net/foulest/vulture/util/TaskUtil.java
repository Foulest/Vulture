package net.foulest.vulture.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.foulest.vulture.Vulture;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

@Getter
@Setter
public class TaskUtil {

    public static void run(@NonNull Runnable runnable) {
        Bukkit.getScheduler().runTask(Vulture.instance, runnable);
    }

    private static BukkitTask taskTimer(@NonNull Runnable runnable, @NonNull Plugin plugin) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, 0L, 1L);
    }

    public static BukkitTask taskTimer(@NonNull Runnable runnable) {
        return taskTimer(runnable, Vulture.instance);
    }

    private static BukkitTask taskAsync(@NonNull Runnable runnable, @NonNull Plugin plugin) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static BukkitTask taskAsync(@NonNull Runnable runnable) {
        return taskAsync(runnable, Vulture.instance);
    }

    private static BukkitTask taskTimerAsync(@NonNull Runnable runnable, @NonNull Plugin plugin) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0L, 1L);
    }

    public static BukkitTask taskTimerAsync(@NonNull Runnable runnable) {
        return taskTimerAsync(runnable, Vulture.instance);
    }

    public static int runSyncRepeating(@NonNull Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(Vulture.instance, runnable, delay, period);
    }
}
