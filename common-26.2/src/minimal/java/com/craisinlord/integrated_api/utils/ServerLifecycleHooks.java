package com.craisinlord.integrated_api.utils;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class ServerLifecycleHooks {
    private static MinecraftServer currentServer;

    private ServerLifecycleHooks() {
    }

    public static void setCurrentServer(@Nullable MinecraftServer server) {
        currentServer = server;
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }
}
