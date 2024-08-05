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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Utility that schedules tasks to be executed on a client response to server tick pings.
 */
@Getter
@ToString
@NoArgsConstructor
public class PingTaskScheduler {

    private final Queue<Queue<PingTask>> scheduledTasks = new ArrayDeque<>();
    private boolean started;

    // Players always log in during the server tick so an end ping is sent first
    @Nullable
    private Queue<PingTask> schedulingTaskQueue = new ArrayDeque<>();
    @Nullable
    private Queue<PingTask> runningTaskQueue = schedulingTaskQueue;

    /**
     * Schedules a runnable to execute when the response for the pings
     * of the current server tick is received from the client.
     * <p>
     *
     * @param task - Runnable to schedule
     */
    public void scheduleTask(PingTask task) {
        // The client can also respond to the start ping before
        // the end ping is sent, meaning tasks should already start
        if (runningTaskQueue != null && runningTaskQueue.equals(schedulingTaskQueue)) {
            task.onStart();
        }

        if (schedulingTaskQueue == null) {
            throw new IllegalStateException("No scheduling task queue present!");
        }

        schedulingTaskQueue.add(task);
    }

    // Called on tick start server ping
    public void onPingSendStart() {
        schedulingTaskQueue = new ArrayDeque<>();
        scheduledTasks.add(schedulingTaskQueue);

        if (!started) {
            started = true;
        }
    }

    // Called on tick end server ping
    public void onPingSendEnd() {
        schedulingTaskQueue = null;
    }

    // Called when tick start server ping response received from client
    public void onPongReceiveStart() {
        runningTaskQueue = scheduledTasks.poll();

        if (runningTaskQueue == null) {
            throw new IllegalStateException("No task queue for pong start!");
        }

        runningTaskQueue.forEach(PingTask::onStart);
    }

    // Called when tick end server ping response received from client
    public void onPongReceiveEnd() {
        if (runningTaskQueue == null) {
            throw new IllegalStateException("No task queue for pong end!");
        }

        while (!runningTaskQueue.isEmpty()) {
            runningTaskQueue.poll().onEnd();
        }

        runningTaskQueue = null;
    }
}
