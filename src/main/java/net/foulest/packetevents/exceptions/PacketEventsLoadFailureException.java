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
package net.foulest.packetevents.exceptions;

import net.foulest.packetevents.PacketEvents;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * An exception thrown by PacketEvents when the {@link PacketEvents#load}
 * method fails to execute without any exceptions, resulting in PacketEvents failing to load.
 *
 * @author retrooper
 * @since 1.7.6
 */
@SuppressWarnings("unused")
public class PacketEventsLoadFailureException extends RuntimeException {

    private static final long serialVersionUID = -3740781881127764470L;

    private PacketEventsLoadFailureException(String message) {
        super(message);
    }

    private PacketEventsLoadFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public PacketEventsLoadFailureException() {
        this("PacketEvents failed to load...");
    }

    public PacketEventsLoadFailureException(Throwable cause) {
        this("PacketEvents failed to load...", cause);
    }

    private void writeObject(@NotNull ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
