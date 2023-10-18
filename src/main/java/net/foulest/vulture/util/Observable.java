package net.foulest.vulture.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@AllArgsConstructor
public final class Observable<T> {

    final Set<ChangeObserver<T>> observers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        T oldValue = this.value;
        this.value = value;
        observers.forEach((it) -> it.handle(oldValue, value));
    }

    public ChangeObserver<T> observe(@NonNull ChangeObserver<T> onChange) {
        observers.add(onChange);
        return onChange;
    }

    public void unobserve(@NonNull ChangeObserver<T> onChange) {
        observers.remove(onChange);
    }

    @FunctionalInterface
    public interface ChangeObserver<T> {

        void handle(T from, T to);
    }
}
