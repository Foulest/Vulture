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
        return PingTask.of(runnable, () -> {
        });
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull PingTask end(Runnable runnable) {
        return PingTask.of(() -> {
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
