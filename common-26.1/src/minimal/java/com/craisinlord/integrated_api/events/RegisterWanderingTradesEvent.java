package com.craisinlord.integrated_api.events;

import com.craisinlord.integrated_api.events.base.EventHandler;
import net.minecraft.world.item.trading.VillagerTrade;

import java.util.function.Consumer;

public record RegisterWanderingTradesEvent(Consumer<VillagerTrade> basic, Consumer<VillagerTrade> rare) {

    public static final EventHandler<RegisterWanderingTradesEvent> EVENT = new EventHandler<>();

    public void addBasicTrade(VillagerTrade trade) {
        basic.accept(trade);
    }

    public void addRareTrade(VillagerTrade trade) {
        rare.accept(trade);
    }
}
