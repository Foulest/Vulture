package io.github.retrooper.packetevents.utils.pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class Pair<A, B> {

    private final A first;
    private final B second;

    @Contract(value = "_, _ -> new", pure = true)
    public static <T, K> @NotNull Pair<T, K> of(T a, K b) {
        return new Pair<>(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }

        Pair b = (Pair) o;
        return Objects.equals(this.first, b.first) && Objects.equals(this.second, b.second);
    }
}
