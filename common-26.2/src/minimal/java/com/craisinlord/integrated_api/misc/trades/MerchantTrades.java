package com.craisinlord.integrated_api.misc.trades;

import com.craisinlord.integrated_api.events.RegisterVillagerTradesEvent;
import com.craisinlord.integrated_api.events.RegisterWanderingTradesEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;
import net.minecraft.world.item.trading.VillagerTrade;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class MerchantTrades {
    private static Map<VillagerProfession, Int2ObjectMap<List<VillagerTrade>>> villagerTrades = Map.of();
    private static List<VillagerTrade> basicWanderingTrades = List.of();
    private static List<VillagerTrade> rareWanderingTrades = List.of();
    private static boolean dirty = true;

    private MerchantTrades() {}

    public static synchronized void invalidate() {
        dirty = true;
    }

    private static synchronized void rebuildIfNeeded() {
        if (!dirty) {
            return;
        }
        dirty = false;

        Map<VillagerProfession, Int2ObjectMap<List<VillagerTrade>>> professionTrades = new IdentityHashMap<>();
        for (VillagerProfession profession : BuiltInRegistries.VILLAGER_PROFESSION) {
            Int2ObjectMap<List<VillagerTrade>> listings = new Int2ObjectOpenHashMap<>();
            RegisterVillagerTradesEvent.EVENT.invoke(new RegisterVillagerTradesEvent(profession, (level, listing) -> listings.computeIfAbsent(level.intValue(), key -> new ArrayList<>()).add(listing)));
            if (!listings.isEmpty()) {
                professionTrades.put(profession, listings);
            }
        }

        List<VillagerTrade> basic = new ArrayList<>();
        List<VillagerTrade> rare = new ArrayList<>();
        RegisterWanderingTradesEvent.EVENT.invoke(new RegisterWanderingTradesEvent(basic::add, rare::add));

        villagerTrades = professionTrades;
        basicWanderingTrades = List.copyOf(basic);
        rareWanderingTrades = List.copyOf(rare);
    }

    public static List<VillagerTrade> getAdditionalTrades(AbstractVillager merchant, ResourceKey<TradeSet> tradeSet) {
        rebuildIfNeeded();
        if (merchant instanceof Villager villager) {
            VillagerData data = villager.getVillagerData();
            VillagerProfession profession = data.profession().value();
            if (tradeSet.equals(profession.getTrades(data.level()))) {
                Int2ObjectMap<List<VillagerTrade>> listings = villagerTrades.get(profession);
                if (listings != null) {
                    return listings.getOrDefault(data.level(), List.of());
                }
            }
        }
        else if (merchant instanceof WanderingTrader) {
            if (tradeSet.equals(TradeSets.WANDERING_TRADER_COMMON)) {
                return basicWanderingTrades;
            }
            if (tradeSet.equals(TradeSets.WANDERING_TRADER_UNCOMMON)) {
                return rareWanderingTrades;
            }
        }
        return List.of();
    }

    public static HolderSet<VillagerTrade> withAdditionalTrades(AbstractVillager merchant, ResourceKey<TradeSet> tradeSet, HolderSet<VillagerTrade> trades) {
        List<VillagerTrade> additionalTrades = getAdditionalTrades(merchant, tradeSet);
        if (additionalTrades.isEmpty()) {
            return trades;
        }

        List<Holder<VillagerTrade>> combined = new ArrayList<>(trades.size() + additionalTrades.size());
        trades.forEach(combined::add);
        for (VillagerTrade trade : additionalTrades) {
            combined.add(Holder.direct(trade));
        }
        return HolderSet.direct(combined);
    }
}
