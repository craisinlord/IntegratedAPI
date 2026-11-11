package com.craisinlord.integrated_api;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class FabricReloadListenerAdapter implements IdentifiableResourceReloadListener {
    private final Identifier id;
    private final ResourceManagerReloadListener listener;

    public FabricReloadListenerAdapter(Identifier id, ResourceManagerReloadListener listener) {
        this.id = id;
        this.listener = listener;
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.SharedState state, Executor prepareExecutor, PreparableReloadListener.PreparationBarrier barrier, Executor applyExecutor) {
        return listener.reload(state, prepareExecutor, barrier, applyExecutor);
    }
}
