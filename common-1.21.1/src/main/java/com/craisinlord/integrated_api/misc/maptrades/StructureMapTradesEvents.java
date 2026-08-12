package com.craisinlord.integrated_api.misc.maptrades;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.events.RegisterVillagerTradesEvent;
import com.craisinlord.integrated_api.events.RegisterWanderingTradesEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

import java.util.List;
import java.util.Map;

public final class StructureMapTradesEvents {
    private StructureMapTradesEvents() {}

    public static void addVillagerTrades(RegisterVillagerTradesEvent event) {
        ResourceLocation currentVillager = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.type());
        if (currentVillager != null && StructureMapManager.STRUCTURE_MAP_MANAGER.VILLAGER_MAP_TRADES.containsKey(currentVillager.toString())) {
            for (VillagerMapObj mapTrade : StructureMapManager.STRUCTURE_MAP_MANAGER.VILLAGER_MAP_TRADES.get(currentVillager.toString())) {
                Holder.Reference<MapDecorationType> icon;
                try {
                    icon = BuiltInRegistries.MAP_DECORATION_TYPE.getHolderOrThrow(ResourceKey.create(Registries.MAP_DECORATION_TYPE, ResourceLocation.tryParse(mapTrade.mapIcon)));
                }
                catch (Exception e) {
                    IntegratedAPI.LOGGER.error(e);
                    icon = BuiltInRegistries.MAP_DECORATION_TYPE.holders().findFirst().get();
                }

                event.addTrade(mapTrade.tradeLevel, new StructureSpecificMaps.TreasureMapForEmeralds(
                        mapTrade.emeraldsRequired,
                        mapTrade.structure,
                        mapTrade.mapName,
                        icon,
                        mapTrade.tradesAllowed,
                        mapTrade.xpReward,
                        mapTrade.spawnRegionSearchRadius));
            }
        }
    }

    public static void addWanderingTrades(RegisterWanderingTradesEvent event) {
        for (Map.Entry<WanderingTraderMapObj.TRADE_TYPE, List<WanderingTraderMapObj>> tradeEntry : StructureMapManager.STRUCTURE_MAP_MANAGER.WANDERING_TRADER_MAP_TRADES.entrySet()) {
            for (WanderingTraderMapObj mapTrade : tradeEntry.getValue()) {
                Holder.Reference<MapDecorationType> icon;
                try {
                    icon = BuiltInRegistries.MAP_DECORATION_TYPE.getHolderOrThrow(ResourceKey.create(Registries.MAP_DECORATION_TYPE, ResourceLocation.tryParse(mapTrade.mapIcon)));
                }
                catch (Exception e) {
                    IntegratedAPI.LOGGER.error(e);
                    icon = BuiltInRegistries.MAP_DECORATION_TYPE.holders().findFirst().get();
                }

                if (tradeEntry.getKey() == WanderingTraderMapObj.TRADE_TYPE.RARE) {
                    event.addRareTrade(new StructureSpecificMaps.TreasureMapForEmeralds(
                            mapTrade.emeraldsRequired,
                            mapTrade.structure,
                            mapTrade.mapName,
                            icon,
                            mapTrade.tradesAllowed,
                            mapTrade.xpReward,
                            mapTrade.spawnRegionSearchRadius));
                }
                else if (tradeEntry.getKey() == WanderingTraderMapObj.TRADE_TYPE.COMMON) {
                    event.addRareTrade(new StructureSpecificMaps.TreasureMapForEmeralds(
                            mapTrade.emeraldsRequired,
                            mapTrade.structure,
                            mapTrade.mapName,
                            icon,
                            mapTrade.tradesAllowed,
                            mapTrade.xpReward,
                            mapTrade.spawnRegionSearchRadius));
                }
            }
        }
    }
}
