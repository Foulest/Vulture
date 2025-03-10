/*
 * This file is part of Pledge - https://github.com/ThomasOM/Pledge
 * Copyright (C) 2021 Thomazz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.foulest.pledge.packet.providers;

import lombok.ToString;
import net.foulest.pledge.packet.PingPacketProvider;
import net.foulest.pledge.util.MinecraftReflection;
import net.foulest.pledge.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@ToString
public class PingPongPacketProvider implements PingPacketProvider {

    private final @NotNull Class<?> pongClass;
    private final @NotNull Field pongIdField;
    private final @NotNull Constructor<?> pingConstructor;

    public PingPongPacketProvider() throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
        pongClass = MinecraftReflection.gamePacket("ServerboundPongPacket");
        pongIdField = ReflectionUtil.getFieldByType(pongClass, int.class);

        @NotNull Class<?> pingClass = MinecraftReflection.gamePacket("ClientboundPingPacket");
        pingConstructor = pingClass.getConstructor(int.class);
    }

    @Override
    public @NotNull Object buildPacket(int id) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return pingConstructor.newInstance(id);
    }

    @Override
    public int idFromPong(Object packet) throws IllegalAccessException {
        return pongIdField.getInt(packet);
    }

    @Override
    public boolean isPong(Object packet) {
        return pongClass.isInstance(packet);
    }

    @Override
    public int getLowerBound() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getUpperBound() {
        return Integer.MAX_VALUE;
    }
}
