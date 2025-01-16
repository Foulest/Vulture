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
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;

@UtilityClass
public class ReflectionUtil {

    @NotNull
    public Field getFieldByClassNames(@NotNull Class<?> clazz, String @NotNull ... simpleNames) throws NoSuchFieldException {
        for (@NotNull String name : simpleNames) {
            for (@NotNull Field field : clazz.getDeclaredFields()) {
                @NotNull String typeSimpleName = field.getType().getSimpleName();

                if (name.equals(typeSimpleName)) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }

        throw new NoSuchFieldException("Could not find field in class "
                + clazz.getName() + " with names " + Arrays.toString(simpleNames));
    }

    public @NotNull Field getFieldByType(@NotNull Class<?> clazz, @NotNull Class<?> type) throws NoSuchFieldException {
        for (@NotNull Field field : clazz.getDeclaredFields()) {
            @NotNull Class<?> foundType = field.getType();

            if (type.isAssignableFrom(foundType)) {
                field.setAccessible(true);
                return field;
            }
        }

        throw new NoSuchFieldException("Could not find field in class "
                + clazz.getName() + " with type " + type.getName());
    }

    @NotNull Object getNonNullFieldByType(@NotNull Object instance, @NotNull Class<?> type) throws ReflectiveOperationException {
        @NotNull Class<?> clazz = instance.getClass();

        for (@NotNull Field field : clazz.getDeclaredFields()) {
            @NotNull Class<?> foundType = field.getType();

            if (type.isAssignableFrom(foundType)) {
                field.setAccessible(true);
                Object o = field.get(instance);

                if (o != null) {
                    return o;
                }
            }
        }

        throw new NoSuchFieldException("Could not find non-null field in class "
                + clazz.getName() + " with type " + type.getName());
    }
}
