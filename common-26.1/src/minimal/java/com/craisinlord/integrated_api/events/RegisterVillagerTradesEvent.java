package com.craisinlord.integrated_api.events;

import com.craisinlord.integrated_api.events.base.EventHandler;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.VillagerTrade;

import java.util.function.BiConsumer;

public record RegisterVillagerTradesEvent(VillagerProfession type, BiConsumer<Integer, VillagerTrade> trade) {

    public static final EventHandler<RegisterVillagerTradesEvent> EVENT = new EventHandler<>();

    public void addTrade(int level, VillagerTrade trade) {
        this.trade.accept(level, trade);
    }
}
