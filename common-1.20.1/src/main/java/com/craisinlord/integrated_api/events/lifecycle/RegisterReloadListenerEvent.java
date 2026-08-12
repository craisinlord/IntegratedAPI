package com.craisinlord.integrated_api.events.lifecycle;

import com.craisinlord.integrated_api.events.base.EventHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.function.BiConsumer;

public record RegisterReloadListenerEvent(BiConsumer<ResourceLocation, PreparableReloadListener> registrar) {

    public static final EventHandler<RegisterReloadListenerEvent> EVENT = new EventHandler<>();

    public void register(ResourceLocation id, PreparableReloadListener listener) {
        registrar.accept(id, listener);
    }
}
