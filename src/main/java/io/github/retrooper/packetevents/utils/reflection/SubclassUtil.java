package io.github.retrooper.packetevents.utils.reflection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public class SubclassUtil {

    public static Class<? extends Enum<?>> getEnumSubClass(Class<?> cls, String name) {
        return (Class<? extends Enum<?>>) getSubClass(cls, name);
    }

    public static Class<? extends Enum<?>> getEnumSubClass(Class<?> cls, int index) {
        return (Class<? extends Enum<?>>) getSubClass(cls, index);
    }

    public static Class<?> getSubClass(Class<?> cls, String name) {
        if (cls == null) {
            return null;
        }

        for (Class<?> subClass : cls.getDeclaredClasses()) {
            if (subClass.getSimpleName().equals(name)) {
                return subClass;
            }
        }
        return null;
    }

    public static Class<?> getSubClass(Class<?> cls, int index) {
        if (cls == null) {
            return null;
        }

        int currentIndex = 0;

        for (Class<?> subClass : cls.getDeclaredClasses()) {
            if (index == currentIndex++) {
                return subClass;
            }
        }
        return null;
    }

    public static @Nullable Class<?> getSubClass(@NotNull Class<?> cls, Annotation annotation, int index) {
        int currentIndex = 0;

        for (Class<?> subClass : cls.getDeclaredClasses()) {
            if (subClass.isAnnotationPresent(annotation.getClass())) {
                if (index == currentIndex++) {
                    return subClass;
                }
            }
        }
        return null;
    }
}
