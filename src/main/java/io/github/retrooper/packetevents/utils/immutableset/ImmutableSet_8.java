package io.github.retrooper.packetevents.utils.immutableset;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ImmutableSet_8<T> extends ImmutableSetAbstract<T> {

    private ImmutableSet<T> immutableSet;

    public ImmutableSet_8() {
        immutableSet = ImmutableSet.<T>builder().build();
    }

    public ImmutableSet_8(List<T> data) {
        immutableSet = ImmutableSet.<T>builder().addAll(data).build();
    }

    @SafeVarargs
    public ImmutableSet_8(T @NotNull ... data) {
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();

        for (T value : data) {
            builder.add(value);
        }

        immutableSet = builder.build();
    }

    @Override
    public boolean contains(T element) {
        return immutableSet.contains(element);
    }

    @Override
    public void add(T element) {
        List<T> elements = new ArrayList<>(immutableSet);
        immutableSet = ImmutableSet.<T>builder().addAll(elements).add(element).build();
    }

    @SafeVarargs
    @Override
    public final void addAll(T... elements) {
        List<T> localElements = new ArrayList<>(immutableSet);
        immutableSet = ImmutableSet.<T>builder().addAll(localElements).addAll(Arrays.asList(elements)).build();
    }
}
