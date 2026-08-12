package com.craisinlord.integrated_api.neoforge;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.events.lifecycle.RegisterReloadListenerEvent;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStartEvent;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStopEvent;
import com.craisinlord.integrated_api.events.lifecycle.SetupEvent;
import com.craisinlord.integrated_api.modinit.registry.ResourcefulRegistries;
import com.craisinlord.integrated_api.modinit.registry.neoforge.ResourcefulRegistriesImpl;
import com.craisinlord.integrated_api.utils.PlatformHooks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(IntegratedAPI.MODID)
public final class IntegratedAPINeoforge {
    public static IEventBus MOD_EVENT_BUS;

    public IntegratedAPINeoforge(IEventBus modEventBus, ModContainer modContainer) {
        MOD_EVENT_BUS = modEventBus;
        modEventBus.addListener(net.neoforged.bus.api.EventPriority.NORMAL, ResourcefulRegistriesImpl::onRegisterForgeRegistries);
        PlatformHooks.init(
                modid -> ModList.get().isLoaded(modid),
                () -> !FMLEnvironment.isProduction()
        );
        ResourcefulRegistries.init(
                ResourcefulRegistriesImpl::create,
                ResourcefulRegistriesImpl::createCustomRegistryInternal
        );
        IntegratedAPI.init();
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) ->
                SetupEvent.EVENT.invoke(new SetupEvent(event::enqueueWork)));
        net.neoforged.bus.api.IEventBus eventBus = net.neoforged.neoforge.common.NeoForge.EVENT_BUS;
        eventBus.addListener((net.neoforged.neoforge.event.server.ServerAboutToStartEvent event) ->
                ServerGoingToStartEvent.EVENT.invoke(new ServerGoingToStartEvent(event.getServer())));
        eventBus.addListener((net.neoforged.neoforge.event.server.ServerStoppingEvent event) ->
                ServerGoingToStopEvent.EVENT.invoke(ServerGoingToStopEvent.INSTANCE));
        eventBus.addListener((net.neoforged.neoforge.event.AddServerReloadListenersEvent event) ->
                RegisterReloadListenerEvent.EVENT.invoke(new RegisterReloadListenerEvent(event::addListener)));
    }
}
