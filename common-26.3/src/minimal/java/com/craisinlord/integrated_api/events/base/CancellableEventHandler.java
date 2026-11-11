package com.craisinlord.integrated_api.events.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CancellableEventHandler<T> {
    private final List<CancellableFunction<T>> listeners = new ArrayList<>();

    public void addListener(CancellableFunction<T> listener) {
        listeners.add(listener);
    }

    public void addListener(CancellableFunctionNoReturn<T> listener) {
        addListener((cancelled, event) -> {
            listener.apply(cancelled, event);
            return false;
        });
    }

    public void addListener(CancellableFunctionOnlyReturn<T> listener) {
        addListener((CancellableFunction<T>) (cancelled, event) -> listener.apply(event));
    }

    public void addListener(Consumer<T> listener) {
        addListener(event -> {
            listener.accept(event);
            return false;
        });
    }

    public void removeListener(CancellableFunction<T> listener) {
        listeners.remove(listener);
    }

    public boolean invoke(T event, boolean cancelled) {
        boolean current = cancelled;
        for (CancellableFunction<T> listener : listeners) {
            if (listener.apply(current, event)) {
                current = true;
            }
        }
        return current;
    }

    public boolean invoke(T event) {
        return invoke(event, false);
    }

    @FunctionalInterface
    public interface CancellableFunction<T> {
        boolean apply(boolean cancelled, T event);
    }

    @FunctionalInterface
    public interface CancellableFunctionNoReturn<T> {
        void apply(boolean cancelled, T event);
    }

    @FunctionalInterface
    public interface CancellableFunctionOnlyReturn<T> {
        boolean apply(T event);
    }
}
