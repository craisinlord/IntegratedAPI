package com.craisinlord.integrated_api.misc.maptrades;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.misc.reload.GsonJsonReloadListener;
import com.craisinlord.integrated_api.misc.trades.MerchantTrades;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StructureMapManager extends GsonJsonReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
    public static final StructureMapManager STRUCTURE_MAP_MANAGER = new StructureMapManager();

    public Map<String, List<VillagerMapObj>> VILLAGER_MAP_TRADES = new HashMap<>();
    public Map<WanderingTraderMapObj.TRADE_TYPE, List<WanderingTraderMapObj>> WANDERING_TRADER_MAP_TRADES = new HashMap<>();

    public StructureMapManager() {
        super(GSON, "integrated_structure_map_trades");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager) {
        Map<String, List<VillagerMapObj>> villagerTrades = new LinkedHashMap<>();
        Map<WanderingTraderMapObj.TRADE_TYPE, List<WanderingTraderMapObj>> wanderingTrades = new LinkedHashMap<>();
        loader.forEach((fileIdentifier, jsonElement) -> {
            try {
                StructureMapCollectionObj spawnerMobEntries = GSON.fromJson(jsonElement, StructureMapCollectionObj.class);
                if (spawnerMobEntries.villagerMaps != null) {
                    spawnerMobEntries.villagerMaps.forEach((profession, trades) -> {
                        if (profession != null && trades != null) {
                            villagerTrades.computeIfAbsent(profession, key -> new ArrayList<>()).addAll(trades);
                        }
                    });
                }
                if (spawnerMobEntries.wanderingTraderMap != null) {
                    spawnerMobEntries.wanderingTraderMap.forEach((tradeType, trades) -> {
                        if (tradeType != null && trades != null) {
                            wanderingTrades.computeIfAbsent(tradeType, key -> new ArrayList<>()).addAll(trades);
                        }
                    });
                }
            }
            catch (Exception e) {
                IntegratedAPI.LOGGER.error("Integrated API Error: Couldn't parse structure map file {}", fileIdentifier, e);
            }
        });
        ImmutableMap.Builder<String, List<VillagerMapObj>> builderVillager = ImmutableMap.builder();
        villagerTrades.forEach((profession, trades) -> builderVillager.put(profession, List.copyOf(trades)));
        ImmutableMap.Builder<WanderingTraderMapObj.TRADE_TYPE, List<WanderingTraderMapObj>> builderWandering = ImmutableMap.builder();
        wanderingTrades.forEach((tradeType, trades) -> builderWandering.put(tradeType, List.copyOf(trades)));
        VILLAGER_MAP_TRADES = builderVillager.build();
        WANDERING_TRADER_MAP_TRADES = builderWandering.build();
        MerchantTrades.invalidate();
    }

    @Override
    protected void onParseError(Identifier fileIdentifier, Exception exception) {
        IntegratedAPI.LOGGER.error("Integrated API Error: Couldn't read structure map file {}", fileIdentifier, exception);
    }
}
