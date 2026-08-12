package com.craisinlord.integrated_api.events;

import com.craisinlord.integrated_api.events.base.EventHandler;

import java.util.function.Consumer;

public record RegisterWanderingTradesEvent(Consumer<Object> basic, Consumer<Object> rare) {

    public static final EventHandler<RegisterWanderingTradesEvent> EVENT = new EventHandler<>();

    public void addBasicTrade(Object trade) {
        basic.accept(trade);
    }

    public void addRareTrade(Object trade) {
        rare.accept(trade);
    }
}
