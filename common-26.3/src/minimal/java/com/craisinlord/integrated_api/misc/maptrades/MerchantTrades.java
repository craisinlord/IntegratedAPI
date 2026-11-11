package com.craisinlord.integrated_api.misc.maptrades;

import com.craisinlord.integrated_api.events.RegisterVillagerTradesEvent;
import com.craisinlord.integrated_api.events.RegisterWanderingTradesEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class MerchantTrades {
    private static volatile Map<ResourceKey<TradeSet>, List<ItemListing>> extraTrades = Map.of();

    private MerchantTrades() {
    }

    public static void rebuild() {
        Map<ResourceKey<TradeSet>, List<ItemListing>> trades = new HashMap<>();
        for (VillagerProfession profession : BuiltInRegistries.VILLAGER_PROFESSION) {
            RegisterVillagerTradesEvent.EVENT.invoke(new RegisterVillagerTradesEvent(profession, (level, listing) -> {
                ResourceKey<TradeSet> tradeSet = profession.getTrades(level);
                if (tradeSet != null && listing != null) {
                    trades.computeIfAbsent(tradeSet, key -> new ArrayList<>()).add(listing);
                }
            }));
        }
        RegisterWanderingTradesEvent.EVENT.invoke(new RegisterWanderingTradesEvent(
                listing -> trades.computeIfAbsent(TradeSets.WANDERING_TRADER_COMMON, key -> new ArrayList<>()).add(listing),
                listing -> trades.computeIfAbsent(TradeSets.WANDERING_TRADER_UNCOMMON, key -> new ArrayList<>()).add(listing)
        ));
        Map<ResourceKey<TradeSet>, List<ItemListing>> immutableTrades = new HashMap<>();
        trades.forEach((key, listings) -> immutableTrades.put(key, List.copyOf(listings)));
        extraTrades = Map.copyOf(immutableTrades);
    }

    public static List<ItemListing> getExtraTrades(ResourceKey<TradeSet> tradeSet) {
        return extraTrades.getOrDefault(tradeSet, List.of());
    }

    public static void addOffers(AbstractVillager merchant, ResourceKey<TradeSet> tradeSet, LootContext lootContext, MerchantOffers offers, HolderSet<VillagerTrade> potentialOffers, int numberOfOffers, boolean allowDuplicates, Runnable original) {
        List<ItemListing> listings = getExtraTrades(tradeSet);
        if (listings.isEmpty()) {
            original.run();
            return;
        }

        List<Supplier<MerchantOffer>> candidates = new ArrayList<>();
        for (Holder<VillagerTrade> villagerTrade : potentialOffers) {
            candidates.add(() -> villagerTrade.value().getOffer(lootContext));
        }
        for (ItemListing listing : listings) {
            candidates.add(() -> listing.getOffer(merchant, lootContext.getRandom()));
        }

        int offersFound = 0;
        while (offersFound < numberOfOffers && !candidates.isEmpty()) {
            int roll = lootContext.getRandom().nextInt(candidates.size());
            MerchantOffer offer = (allowDuplicates ? candidates.get(roll) : candidates.remove(roll)).get();
            if (offer == null) {
                if (allowDuplicates) {
                    candidates.remove(roll);
                }
            } else {
                offers.add(offer);
                offersFound++;
            }
        }
    }
}
