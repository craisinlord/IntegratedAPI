package com.craisinlord.integrated_api;

import com.craisinlord.integrated_api.events.lifecycle.RegisterReloadListenerEvent;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStartEvent;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStopEvent;
import com.craisinlord.integrated_api.modinit.registry.ResourcefulRegistries;
import com.craisinlord.integrated_api.modinit.registry.fabric.ResourcefulRegistriesImpl;
import com.craisinlord.integrated_api.utils.PlatformHooks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.fabricmc.loader.api.FabricLoader;

public class IntegratedAPIFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PlatformHooks.init(
                modid -> FabricLoader.getInstance().isModLoaded(modid),
                () -> FabricLoader.getInstance().isDevelopmentEnvironment()
        );
        ResourcefulRegistries.init(
                ResourcefulRegistriesImpl::create,
                (key, modId, save, sync, allowModification) -> ResourcefulRegistriesImpl.createCustomRegistryInternal(modId, key, save, sync, allowModification)
        );
        IntegratedAPI.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ServerGoingToStartEvent.EVENT.invoke(new ServerGoingToStartEvent(server));
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
                ServerGoingToStopEvent.EVENT.invoke(ServerGoingToStopEvent.INSTANCE));

        RegisterReloadListenerEvent.EVENT.invoke(new RegisterReloadListenerEvent((id, listener) ->
                ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricReloadListener(id, listener))));
    }
}
