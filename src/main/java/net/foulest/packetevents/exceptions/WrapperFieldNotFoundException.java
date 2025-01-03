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

import net.foulest.packetevents.packetwrappers.WrappedPacket;
import net.foulest.packetevents.utils.reflection.ClassUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * An exception thrown by PacketEvents when a wrapper fails to find a field.
 *
 * @author retrooper
 * @see WrappedPacket#read(int, Class)
 * @see WrappedPacket#write(Class, int, Object)
 * @since 1.6.9
 */
@SuppressWarnings("unused")
public class WrapperFieldNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 4872909960607964761L;

    public WrapperFieldNotFoundException(String message) {
        super(message);
    }

    public WrapperFieldNotFoundException(@NotNull Class<?> packetClass, Class<?> type, int index) {
        this("PacketEvents failed to find a " + ClassUtil.getClassSimpleName(type)
                + " indexed " + index + " by its type in the " + packetClass.getName() + " class!");
    }

    private void writeObject(@NotNull ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
