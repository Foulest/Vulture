package io.github.retrooper.packetevents.utils.enums;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnumUtil {

    public static @Nullable Enum<?> valueOf(@NotNull Class<? extends Enum<?>> cls, String constantName) {
        for (Enum<?> enumConstant : cls.getEnumConstants()) {
            if (enumConstant.name().equals(constantName)) {
                return enumConstant;
            }
        }
        return null;
    }

    public static Enum<?> valueByIndex(@NotNull Class<? extends Enum<?>> cls, int index) {
        return cls.getEnumConstants()[index];
    }
}
