package com.craisinlord.integrated_api;

import com.craisinlord.integrated_api.events.RegisterVillagerTradesEvent;
import com.craisinlord.integrated_api.events.RegisterWanderingTradesEvent;
import com.craisinlord.integrated_api.events.lifecycle.RegisterReloadListenerEvent;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStartEvent;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStopEvent;
import com.craisinlord.integrated_api.events.lifecycle.SetupEvent;
import com.craisinlord.integrated_api.misc.maptrades.StructureMapManager;
import com.craisinlord.integrated_api.misc.maptrades.StructureMapTradesEvents;
import com.craisinlord.integrated_api.misc.mobspawners.MobSpawnerManager;
import com.craisinlord.integrated_api.misc.structurepiececounter.StructurePieceCountsManager;
import com.craisinlord.integrated_api.misc.workstations.WorkstationManager;
import com.craisinlord.integrated_api.modinit.*;
import com.craisinlord.integrated_api.utils.AsyncLocator;
import com.craisinlord.integrated_api.utils.ServerLifecycleHooks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class IntegratedAPI {
    public static final String MODID = "integrated_api";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().setLenient().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();


    public static final int NEW_STRUCTURE_SIZE = 512;

    public static void init() {
        IATags.initTags();
        IAPredicates.RULE_TEST.init();
        IAPredicates.POS_RULE_TEST.init();
        IAStructures.STRUCTURE_TYPE.init();
        IAPlacements.PLACEMENT_MODIFIER.init();
        IAProcessors.STRUCTURE_PROCESSOR.init();
        IAStructurePieces.STRUCTURE_PIECE.init();
        IAStructurePieces.STRUCTURE_POOL_ELEMENT.init();
        IAStructurePlacementType.STRUCTURE_PLACEMENT_TYPE.init();
        IAConditionsRegistry.IA_JSON_CONDITIONS_REGISTRY.init();

        SetupEvent.EVENT.addListener(IntegratedAPI::setup);
        RegisterReloadListenerEvent.EVENT.addListener(IntegratedAPI::registerDatapackListener);
        ServerGoingToStartEvent.EVENT.addListener(IntegratedAPI::serverAboutToStart);
        ServerGoingToStopEvent.EVENT.addListener(IntegratedAPI::onServerStopping);
        RegisterVillagerTradesEvent.EVENT.addListener(IntegratedAPI::onAddVillagerTrades);
        RegisterWanderingTradesEvent.EVENT.addListener(IntegratedAPI::onWanderingTrades);
    }

    private static void setup(final SetupEvent event) {
    }

    private static void serverAboutToStart(final ServerGoingToStartEvent event) {
        ServerLifecycleHooks.setCurrentServer(event.getServer());
        AsyncLocator.handleServerAboutToStartEvent();
    }

    private static void onServerStopping(final ServerGoingToStopEvent event) {
        ServerLifecycleHooks.setCurrentServer(null);
        AsyncLocator.handleServerStoppingEvent();
    }

    private static void onAddVillagerTrades(final RegisterVillagerTradesEvent event) {
        StructureMapTradesEvents.addVillagerTrades(event);
    }

    private static void onWanderingTrades(final RegisterWanderingTradesEvent event) {
        StructureMapTradesEvents.addWanderingTrades(event);
    }

    public static void registerDatapackListener(final RegisterReloadListenerEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(IntegratedAPI.MODID, "integrated_structure_spawners"), MobSpawnerManager.MOB_SPAWNER_MANAGER);
        event.register(ResourceLocation.fromNamespaceAndPath(IntegratedAPI.MODID, "integrated_structure_map_trades"), StructureMapManager.STRUCTURE_MAP_MANAGER);
        event.register(ResourceLocation.fromNamespaceAndPath(IntegratedAPI.MODID, "integrated_pieces_spawn_counts"), StructurePieceCountsManager.STRUCTURE_PIECE_COUNTS_MANAGER);
        event.register(ResourceLocation.fromNamespaceAndPath(IntegratedAPI.MODID, "integrated_workstations"), WorkstationManager.WORKSTATION_MANAGER);
    }
}
