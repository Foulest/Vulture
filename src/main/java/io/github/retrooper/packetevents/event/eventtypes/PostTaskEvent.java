package io.github.retrooper.packetevents.event.eventtypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Every event that allows you to schedule a task that will run after the action associated with the event.
 *
 * @author retrooper
 * @since 1.8
 */
public interface PostTaskEvent {

    boolean isPostTaskAvailable();

    @Nullable
    Runnable getPostTask();

    void setPostTask(@NotNull Runnable postTask);
}
