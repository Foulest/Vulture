package io.github.retrooper.packetevents.utils.pair;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class MojangPairUtils {

    public static @NotNull Pair<Object, Object> extractPair(Object mojangPairObj) {
        Pair<Object, Object> mojangPair = (Pair<Object, Object>) mojangPairObj;
        return Pair.of(mojangPair.getFirst(), mojangPair.getSecond());
    }

    @Contract("_, _ -> new")
    public static @NotNull Object getMojangPair(Object first, Object second) {
        return new Pair<>(first, second);
    }

    @Contract("_ -> new")
    public static @NotNull Object getMojangPair(@NotNull Pair<Object, Object> pair) {
        return getMojangPair(pair.getFirst(), pair.getSecond());
    }
}
