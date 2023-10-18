package net.foulest.vulture.util.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Objects;

/**
 * A pair of objects.
 *
 * @param <X> The type of the first object.
 * @param <Y> The type of the second object.
 * @author Foulest
 * @project Vulture
 */
@Getter
@Setter
@AllArgsConstructor
public final class Pair<X, Y> {

    private final X x;
    private final Y y;

    public static <X, Y> Pair<X, Y> of(@NonNull X x, @NonNull Y y) {
        return new Pair<>(x, y);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(@NonNull Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }

        Pair<X, Y> pair = (Pair<X, Y>) obj;
        return Objects.equals(x, pair.x) && Objects.equals(y, pair.y);
    }
}
