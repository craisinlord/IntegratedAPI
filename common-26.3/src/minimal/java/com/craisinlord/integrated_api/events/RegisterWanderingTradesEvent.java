package com.craisinlord.integrated_api.events;

import com.craisinlord.integrated_api.events.base.EventHandler;
import com.craisinlord.integrated_api.misc.maptrades.ItemListing;

import java.util.function.Consumer;

public record RegisterWanderingTradesEvent(Consumer<ItemListing> basic, Consumer<ItemListing> rare) {

    public static final EventHandler<RegisterWanderingTradesEvent> EVENT = new EventHandler<>();

    public void addBasicTrade(ItemListing trade) {
        basic.accept(trade);
    }

    public void addRareTrade(ItemListing trade) {
        rare.accept(trade);
    }
}
