package com.craisinlord.integrated_api.neoforge;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.events.lifecycle.RegisterReloadListenerEvent;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStartEvent;
import com.craisinlord.integrated_api.events.lifecycle.ServerGoingToStopEvent;
import com.craisinlord.integrated_api.events.lifecycle.SetupEvent;
import com.craisinlord.integrated_api.modinit.neoforge.IABiomeModifiers;
import com.craisinlord.integrated_api.modinit.registry.ResourcefulRegistries;
import com.craisinlord.integrated_api.modinit.registry.neoforge.ResourcefulRegistriesImpl;
import com.craisinlord.integrated_api.utils.PlatformHooks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;


@Mod(IntegratedAPI.MODID)
public class IntegratedAPINeoforge {

    public static net.neoforged.bus.api.IEventBus modEventBusTempHolder = null;

    public IntegratedAPINeoforge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(net.neoforged.bus.api.EventPriority.NORMAL, ResourcefulRegistriesImpl::onRegisterForgeRegistries);

        modEventBusTempHolder = modEventBus;
        PlatformHooks.init(
                modid -> ModList.get().isLoaded(modid),
                () -> !FMLEnvironment.production
        );
        ResourcefulRegistries.init(
                ResourcefulRegistriesImpl::create,
                (key, modId, save, sync, allowModification) -> ResourcefulRegistriesImpl.createCustomRegistryInternal(modId, key, save, sync, allowModification)
        );
        IntegratedAPI.init();
        modEventBusTempHolder = null;
        IABiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);

        modEventBus.addListener(IntegratedAPINeoforge::onSetup);

        net.neoforged.bus.api.IEventBus eventBus = NeoForge.EVENT_BUS;
        eventBus.addListener(IntegratedAPINeoforge::onServerStarting);
        eventBus.addListener(IntegratedAPINeoforge::onServerStopping);
        eventBus.addListener(IntegratedAPINeoforge::onAddReloadListeners);
    }

    private static void onSetup(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        SetupEvent.EVENT.invoke(new SetupEvent(event::enqueueWork));
    }

    private static void onServerStarting(net.neoforged.neoforge.event.server.ServerAboutToStartEvent event) {
        ServerGoingToStartEvent.EVENT.invoke(new ServerGoingToStartEvent(event.getServer()));
    }

    private static void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        ServerGoingToStopEvent.EVENT.invoke(ServerGoingToStopEvent.INSTANCE);
    }

    private static void onAddReloadListeners(net.neoforged.neoforge.event.AddReloadListenerEvent event) {
        RegisterReloadListenerEvent.EVENT.invoke(new RegisterReloadListenerEvent((id, listener) -> event.addListener(listener)));
    }
}
