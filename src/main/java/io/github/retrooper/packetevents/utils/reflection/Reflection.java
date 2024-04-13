package io.github.retrooper.packetevents.utils.reflection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Reflection {

    // FIELDS

    public static Field @NotNull [] getFields(@NotNull Class<?> cls) {
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field f : declaredFields) {
            f.setAccessible(true);
        }
        return declaredFields;
    }

    public static @Nullable Field getField(Class<?> cls, String name) {
        for (Field f : getFields(cls)) {
            if (f.getName().equals(name)) {
                return f;
            }
        }

        if (cls.getSuperclass() != null) {
            return getField(cls.getSuperclass(), name);
        }
        return null;
    }

    public static Field getField(Class<?> cls, Class<?> dataType, int index) {
        if (dataType == null || cls == null) {
            return null;
        }

        int currentIndex = 0;

        for (Field f : getFields(cls)) {
            if (dataType.isAssignableFrom(f.getType())) {
                if (currentIndex++ == index) {
                    return f;
                }
            }
        }

        if (cls.getSuperclass() != null) {
            return getField(cls.getSuperclass(), dataType, index);
        }
        return null;
    }

    public static Field getField(Class<?> cls, Class<?> dataType, int index, boolean ignoreStatic) {
        if (dataType == null || cls == null) {
            return null;
        }

        int currentIndex = 0;

        for (Field f : getFields(cls)) {
            if (dataType.isAssignableFrom(f.getType()) && (!ignoreStatic || !Modifier.isStatic(f.getModifiers()))) {
                if (currentIndex++ == index) {
                    return f;
                }
            }
        }

        if (cls.getSuperclass() != null) {
            return getField(cls.getSuperclass(), dataType, index);
        }
        return null;
    }

    public static @Nullable Field getField(Class<?> cls, int index) {
        try {
            return getFields(cls)[index];
        } catch (Exception ex) {
            if (cls.getSuperclass() != null) {
                return getFields(cls.getSuperclass())[index];
            }
        }
        return null;
    }

    // METHODS

    public static @NotNull List<Method> getMethods(@NotNull Class<?> cls, String name, Class<?>... params) {
        List<Method> methods = new ArrayList<>();

        for (Method m : cls.getDeclaredMethods()) {
            if ((params == null || Arrays.equals(m.getParameterTypes(), params)) && name.equals(m.getName())) {
                m.setAccessible(true);
                methods.add(m);
            }
        }
        return methods;
    }

    public static @Nullable Method getMethod(@NotNull Class<?> cls, int index, Class<?>... params) {
        int currentIndex = 0;

        for (Method m : cls.getDeclaredMethods()) {
            if ((params == null || Arrays.equals(m.getParameterTypes(), params)) && index == currentIndex++) {
                m.setAccessible(true);
                return m;
            }
        }

        if (cls.getSuperclass() != null) {
            return getMethod(cls.getSuperclass(), index, params);
        }
        return null;
    }

    public static @Nullable Method getMethod(@NotNull Class<?> cls, Class<?> returning, int index, Class<?>... params) {
        int currentIndex = 0;

        for (Method m : cls.getDeclaredMethods()) {
            if (Arrays.equals(m.getParameterTypes(), params)
                    && (returning == null || m.getReturnType().equals(returning))
                    && index == currentIndex++) {
                m.setAccessible(true);
                return m;
            }
        }

        if (cls.getSuperclass() != null) {
            return getMethod(cls.getSuperclass(), null, index, params);
        }
        return null;
    }

    public static @Nullable Method getMethod(@NotNull Class<?> cls, String name, Class<?> returning, Class<?>... params) {
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name)
                    && Arrays.equals(m.getParameterTypes(), params) &&
                    (returning == null || m.getReturnType().equals(returning))) {
                m.setAccessible(true);
                return m;
            }
        }

        if (cls.getSuperclass() != null) {
            return getMethod(cls.getSuperclass(), name, null, params);
        }
        return null;
    }

    public static Method getMethod(Class<?> cls, String name, int index) {
        if (cls == null) {
            return null;
        }

        int currentIndex = 0;

        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name) && index == currentIndex++) {
                m.setAccessible(true);
                return m;
            }
        }

        if (cls.getSuperclass() != null) {
            return getMethod(cls.getSuperclass(), name, index);
        }
        return null;
    }

    public static Method getMethod(Class<?> cls, Class<?> returning, int index) {
        if (cls == null) {
            return null;
        }

        int currentIndex = 0;

        for (Method m : cls.getDeclaredMethods()) {
            if ((returning == null || m.getReturnType().equals(returning)) && index == currentIndex++) {
                m.setAccessible(true);
                return m;
            }
        }

        if (cls.getSuperclass() != null) {
            return getMethod(cls.getSuperclass(), returning, index);
        }
        return null;
    }

    public static Method getMethodCheckContainsString(Class<?> cls, String nameContainsThisStr, Class<?> returning) {
        if (cls == null) {
            return null;
        }

        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().contains(nameContainsThisStr) && (returning == null || m.getReturnType().equals(returning))) {
                m.setAccessible(true);
                return m;
            }
        }

        if (cls.getSuperclass() != null) {
            return getMethodCheckContainsString(cls.getSuperclass(), nameContainsThisStr, returning);
        }
        return null;
    }

    public static Method getMethod(Class<?> cls, String name, Class<?> returning) {
        if (cls == null) {
            return null;
        }

        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name)
                    && (returning == null || m.getReturnType().equals(returning))) {
                m.setAccessible(true);
                return m;
            }
        }

        if (cls.getSuperclass() != null) {
            return getMethod(cls.getSuperclass(), name, returning);
        }
        return null;
    }

    public static @Nullable Class<?> getClassByNameWithoutException(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
