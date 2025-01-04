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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unchecked")
public final class TickEndTask {

    private static final List<Object> runnables;
    private static final Class<?> runnableClass;

    static {
        try {
            Server server = Bukkit.getServer();
            Object mcServer = server.getClass().getMethod("getServer").invoke(server);
            Field field = ReflectionUtil.getFieldByType(mcServer.getClass().getSuperclass(), List.class);
            runnables = (List<Object>) field.get(mcServer);
            runnableClass = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        } catch (NoSuchMethodException | NoSuchFieldException
                 | InvocationTargetException | IllegalAccessException ex) {
            throw new RuntimeException("Could not set up tick end runnable!", ex);
        }
    }

    private final Runnable runnable;
    private final AtomicReference<Object> registeredObject = new AtomicReference<>();

    private TickEndTask start() {
        if (registeredObject.get() != null) {
            throw new IllegalStateException("Already registered!");
        }

        // Hack to add runnable to tickables
        Object newObject;

        if (Runnable.class.isAssignableFrom(runnableClass)) {
            newObject = runnable;
        } else {
            Object handle = new Object();

            newObject = Proxy.newProxyInstance(runnableClass.getClassLoader(), new Class[]{runnableClass},
                    (proxy, method, args) -> {
                        Class<?> declaring = method.getDeclaringClass();

                        if (declaring.equals(Object.class)) {
                            return method.invoke(handle, args);
                        } else {
                            runnable.run();
                            return null;
                        }
                    }
            );
        }

        if (!registeredObject.compareAndSet(null, newObject)) {
            throw new IllegalStateException("Already registered!");
        }

        runnables.add(registeredObject.get());
        return this;
    }

    public void cancel() {
        Object currentObject = registeredObject.getAndSet(null);

        if (currentObject == null) {
            throw new IllegalStateException("Not registered yet!");
        }

        runnables.remove(currentObject);
    }

    public static TickEndTask create(Runnable runnable) {
        return new TickEndTask(runnable).start();
    }
}
