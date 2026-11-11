package com.craisinlord.integrated_api.misc.maptrades;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.events.RegisterVillagerTradesEvent;
import com.craisinlord.integrated_api.events.RegisterWanderingTradesEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StructureMapTradesEvents {
    private StructureMapTradesEvents() {}

    public static void addVillagerTrades(RegisterVillagerTradesEvent event) {
        Identifier currentVillager = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.type());
        if (currentVillager != null && StructureMapManager.STRUCTURE_MAP_MANAGER.VILLAGER_MAP_TRADES.containsKey(currentVillager.toString())) {
            for (VillagerMapObj mapTrade : StructureMapManager.STRUCTURE_MAP_MANAGER.VILLAGER_MAP_TRADES.get(currentVillager.toString())) {
                event.addTrade(mapTrade.tradeLevel, new StructureSpecificMaps.TreasureMapForEmeralds(
                        mapTrade.emeraldsRequired,
                        mapTrade.structure,
                        mapTrade.mapName,
                        getMapIcon(mapTrade.mapIcon),
                        mapTrade.tradesAllowed,
                        mapTrade.xpReward,
                        mapTrade.spawnRegionSearchRadius));
            }
        }
    }

    public static void addWanderingTrades(RegisterWanderingTradesEvent event) {
        for (Map.Entry<WanderingTraderMapObj.TRADE_TYPE, List<WanderingTraderMapObj>> tradeEntry : StructureMapManager.STRUCTURE_MAP_MANAGER.WANDERING_TRADER_MAP_TRADES.entrySet()) {
            for (WanderingTraderMapObj mapTrade : tradeEntry.getValue()) {
                StructureSpecificMaps.TreasureMapForEmeralds trade = new StructureSpecificMaps.TreasureMapForEmeralds(
                        mapTrade.emeraldsRequired,
                        mapTrade.structure,
                        mapTrade.mapName,
                        getMapIcon(mapTrade.mapIcon),
                        mapTrade.tradesAllowed,
                        mapTrade.xpReward,
                        mapTrade.spawnRegionSearchRadius);

                if (tradeEntry.getKey() == WanderingTraderMapObj.TRADE_TYPE.RARE) {
                    event.addRareTrade(trade);
                }
                else if (tradeEntry.getKey() == WanderingTraderMapObj.TRADE_TYPE.COMMON) {
                    event.addBasicTrade(trade);
                }
            }
        }
    }

    private static Holder.Reference<MapDecorationType> getMapIcon(String mapIcon) {
        try {
            Identifier iconId = Identifier.tryParse(mapIcon);
            if (iconId == null) {
                iconId = Identifier.tryParse(mapIcon.toLowerCase(Locale.ROOT));
            }
            return BuiltInRegistries.MAP_DECORATION_TYPE.getOrThrow(ResourceKey.create(Registries.MAP_DECORATION_TYPE, iconId));
        }
        catch (Exception e) {
            IntegratedAPI.LOGGER.error("Integrated API Error: Invalid map icon {}", mapIcon, e);
            return BuiltInRegistries.MAP_DECORATION_TYPE.listElements().findFirst().get();
        }
    }
}
