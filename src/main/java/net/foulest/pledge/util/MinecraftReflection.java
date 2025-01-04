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
package net.foulest.pledge.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@UtilityClass
public class MinecraftReflection {

    private final String BASE = Bukkit.getServer().getClass().getPackage().getName();
    private final String NMS = BASE.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    public Class<?> gamePacket(String className) throws ClassNotFoundException {
        try {
            return Class.forName(NMS + "." + className); // Legacy structure
        } catch (ClassNotFoundException ignored) {
        }

        try {
            return Class.forName("net.minecraft.network.protocol.game." + className); // Game packet
        } catch (ClassNotFoundException ignored) {
        }

        try {
            return Class.forName("net.minecraft.network.protocol.common." + className); // 1.20.2+ common packets
        } catch (ClassNotFoundException ignored) {
        }

        throw new ClassNotFoundException("Game packet class not found!");
    }

    Class<?> getMinecraftClass(String... names) {
        String[] packageNames = {
                getMinecraftPackage(),
                getMinecraftPackageLegacy()
        };

        for (String packageName : packageNames) {
            for (String name : names) {
                try {
                    return Class.forName(packageName + "." + name);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        throw new RuntimeException("Could not find minecraft class: " + Arrays.toString(names));
    }

    private @NotNull String getCraftBukkitPackage() {
        return Bukkit.getServer().getClass().getPackage().getName();
    }

    @Contract(pure = true)
    private @NotNull String getMinecraftPackage() {
        return "net.minecraft";
    }

    private @NotNull String getMinecraftPackageLegacy() {
        return getCraftBukkitPackage().replace("org.bukkit.craftbukkit", "net.minecraft.server");
    }

    Object getServerConnection() throws NoSuchFieldException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Object minecraftServer = Bukkit.getServer().getClass().getDeclaredMethod("getServer").invoke(Bukkit.getServer());
        Field connectionField = ReflectionUtil.getFieldByClassNames(minecraftServer.getClass().getSuperclass(), "ServerConnectionListener", "ServerConnection");
        return connectionField.get(minecraftServer);
    }
}
