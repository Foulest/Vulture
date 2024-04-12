package io.github.retrooper.packetevents.utils.immutableset;

abstract class ImmutableSetAbstract<T> {

    public abstract boolean contains(T element);

    public abstract void add(T element);

    public abstract void addAll(T... element);
}
