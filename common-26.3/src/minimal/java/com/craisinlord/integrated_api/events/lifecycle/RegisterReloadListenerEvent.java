package com.craisinlord.integrated_api.events.lifecycle;

import com.craisinlord.integrated_api.events.base.EventHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.util.function.BiConsumer;

public record RegisterReloadListenerEvent(BiConsumer<Identifier, ResourceManagerReloadListener> registrar) {
    public static final EventHandler<RegisterReloadListenerEvent> EVENT = new EventHandler<>();

    public void register(Identifier id, ResourceManagerReloadListener listener) {
        registrar.accept(id, listener);
    }
}
