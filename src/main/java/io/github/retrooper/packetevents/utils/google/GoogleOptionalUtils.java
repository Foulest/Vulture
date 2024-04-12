package io.github.retrooper.packetevents.utils.google;

import com.google.common.base.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class GoogleOptionalUtils {

    public static Object getOptionalValue(Object opt) {
        return ((Optional<?>) opt).get();
    }

    public static Object getOptionalEmpty() {
        return Optional.absent();
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Object getOptional(Object value) {
        return Optional.of(value);
    }
}
