package com.craisinlord.integrated_api.utils;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public class ServerLifecycleHooks {
    private static volatile MinecraftServer currentServer;

    public static void setCurrentServer(@Nullable MinecraftServer server) {
        currentServer = server;
    }

    public static @Nullable MinecraftServer getCurrentServer() {
        return currentServer;
    }
}
