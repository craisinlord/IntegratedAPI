package com.craisinlord.integrated_api.events.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class EventHandler<T> {
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public void addListener(Consumer<T> listener) {
        listeners.add(listener);
    }

    public void invoke(T event) {
        for (Consumer<T> listener : listeners) {
            listener.accept(event);
        }
    }
}
