package com.craisinlord.integrated_api;

import com.craisinlord.integrated_api.events.lifecycle.RegisterReloadListenerEvent;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStartEvent;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStopEvent;
import com.craisinlord.integrated_api.events.lifecycle.SetupEvent;
import com.craisinlord.integrated_api.misc.mobspawners.MobSpawnerManager;
import com.craisinlord.integrated_api.misc.structurepiececounter.StructurePieceCountsManager;
import com.craisinlord.integrated_api.misc.workstations.WorkstationManager;
import com.craisinlord.integrated_api.modinit.IAConditionsRegistry;
import com.craisinlord.integrated_api.modinit.IAPlacements;
import com.craisinlord.integrated_api.modinit.IAPredicates;
import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.craisinlord.integrated_api.modinit.IAStructurePlacementType;
import com.craisinlord.integrated_api.modinit.IAStructurePieces;
import com.craisinlord.integrated_api.modinit.IAStructures;
import com.craisinlord.integrated_api.modinit.IATags;
import com.craisinlord.integrated_api.utils.AsyncLocator;
import com.craisinlord.integrated_api.utils.ServerLifecycleHooks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IntegratedAPI {
    public static final String MODID = "integrated_api";
    public static final int NEW_STRUCTURE_SIZE = 512;
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    private static boolean initialized;

    private IntegratedAPI() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        IATags.initTags();
        IAConditionsRegistry.IA_JSON_CONDITIONS_REGISTRY.init();
        IAPlacements.PLACEMENT_MODIFIER.init();
        IAPredicates.RULE_TEST.init();
        IAPredicates.POS_RULE_TEST.init();
        IAProcessors.STRUCTURE_PROCESSOR.init();
        IAStructurePlacementType.STRUCTURE_PLACEMENT_TYPE.init();
        IAStructurePieces.STRUCTURE_POOL_ELEMENT.init();
        IAStructurePieces.STRUCTURE_PIECE.init();
        IAStructures.STRUCTURE_TYPE.init();
        SetupEvent.EVENT.addListener(IntegratedAPI::setup);
        RegisterReloadListenerEvent.EVENT.addListener(IntegratedAPI::registerDatapackListener);
        ServerGoingToStartEvent.EVENT.addListener(IntegratedAPI::serverAboutToStart);
        ServerGoingToStopEvent.EVENT.addListener(IntegratedAPI::onServerStopping);
        LOGGER.info("Initialized Integrated API 26.2 bootstrap");
    }

    private static void setup(SetupEvent event) {
    }

    private static void serverAboutToStart(ServerGoingToStartEvent event) {
        ServerLifecycleHooks.setCurrentServer(event.server());
        AsyncLocator.handleServerAboutToStartEvent();
    }

    private static void onServerStopping(ServerGoingToStopEvent event) {
        ServerLifecycleHooks.setCurrentServer(null);
        AsyncLocator.handleServerStoppingEvent();
    }

    private static void registerDatapackListener(RegisterReloadListenerEvent event) {
        event.register(Identifier.fromNamespaceAndPath(MODID, "integrated_structure_spawners"), MobSpawnerManager.MOB_SPAWNER_MANAGER);
        event.register(Identifier.fromNamespaceAndPath(MODID, "integrated_pieces_spawn_counts"), StructurePieceCountsManager.STRUCTURE_PIECE_COUNTS_MANAGER);
        event.register(Identifier.fromNamespaceAndPath(MODID, "integrated_workstations"), WorkstationManager.WORKSTATION_MANAGER);
    }
}
