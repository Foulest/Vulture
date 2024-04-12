package io.github.retrooper.packetevents.utils.immutableset;

import java.util.List;

public class ImmutableSetCustom<T> {

    private final ImmutableSetAbstract<T> immutableSetAbstract;

    public ImmutableSetCustom() {
        immutableSetAbstract = new ImmutableSet_8<>();
    }

    public ImmutableSetCustom(List<T> data) {
        immutableSetAbstract = new ImmutableSet_8<>(data);
    }

    @SafeVarargs
    public ImmutableSetCustom(T... data) {
        immutableSetAbstract = new ImmutableSet_8<>(data);
    }

    public boolean contains(T element) {
        return immutableSetAbstract.contains(element);
    }

    public void add(T element) {
        immutableSetAbstract.add(element);
    }

    @SafeVarargs
    public final void addAll(T... elements) {
        immutableSetAbstract.addAll(elements);
    }
}
