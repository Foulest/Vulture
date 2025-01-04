/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
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
package net.foulest.packetevents.event.eventtypes;

import org.jetbrains.annotations.NotNull;

/**
 * Every event that allows you to schedule a task that will run after the action associated with the event.
 *
 * @author retrooper
 * @since 1.8
 */
public interface PostTaskEvent {

    boolean isPostTaskAvailable();

    Runnable getPostTask();

    void setPostTask(@NotNull Runnable postTask);
}
