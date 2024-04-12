package io.github.retrooper.packetevents.utils.google;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OptionalUtils {

    @Contract("_ -> new")
    public static @NotNull Optional<?> convertToJavaOptional(Object googleOptional) {
        return Optional.of(GoogleOptionalUtils.getOptionalValue(googleOptional));
    }
}
