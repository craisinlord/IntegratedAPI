package com.craisinlord.integrated_api.misc.maptrades;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.craisinlord.integrated_api.IntegratedAPI;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureMapManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().setLenient().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
    public final static StructureMapManager STRUCTURE_MAP_MANAGER = new StructureMapManager();

    public Map<String, List<VillagerMapObj>> VILLAGER_MAP_TRADES = new HashMap<>();
    public Map<WanderingTraderMapObj.TRADE_TYPE, List<WanderingTraderMapObj>> WANDERING_TRADER_MAP_TRADES = new HashMap<>();

    public StructureMapManager() {
        super(GSON, "integrated_structure_map_trades");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, ProfilerFiller profiler) {
        VILLAGER_MAP_TRADES = ImmutableMap.of();
        WANDERING_TRADER_MAP_TRADES = ImmutableMap.of();
    }
}
