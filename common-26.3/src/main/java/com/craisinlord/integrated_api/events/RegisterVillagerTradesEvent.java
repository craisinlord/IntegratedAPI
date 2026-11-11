package com.craisinlord.integrated_api.events;

import com.craisinlord.integrated_api.events.base.EventHandler;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import java.util.function.BiConsumer;

public record RegisterVillagerTradesEvent(VillagerProfession type, BiConsumer<Integer, Object> trade) {

    public static final EventHandler<RegisterVillagerTradesEvent> EVENT = new EventHandler<>();

    public void addTrade(int level, Object trade) {
        this.trade.accept(level, trade);
    }
}
