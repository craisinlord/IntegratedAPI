package com.craisinlord.integrated_api.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class AsyncLocator {
    private static ExecutorService locatingExecutorService;

    private AsyncLocator() {
    }

    private static void setupExecutorService() {
        shutdownExecutorService();
        locatingExecutorService = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private static final AtomicInteger poolNum = new AtomicInteger(1);
            private final AtomicInteger threadNum = new AtomicInteger(1);
            private final String namePrefix = "integratedapi-" + poolNum.getAndIncrement() + "-thread-";

            @Override
            public Thread newThread(@NotNull Runnable runnable) {
                return new Thread(null, runnable, namePrefix + threadNum.getAndIncrement());
            }
        });
    }

    private static void shutdownExecutorService() {
        if (locatingExecutorService != null) {
            locatingExecutorService.shutdown();
            locatingExecutorService = null;
        }
    }

    public static void handleServerAboutToStartEvent() {
        setupExecutorService();
    }

    public static void handleServerStoppingEvent() {
        shutdownExecutorService();
    }

    public static LocateTask<BlockPos> locate(ServerLevel level, TagKey<Structure> structureTag, BlockPos pos, int searchRadius, boolean skipKnownStructures) {
        CompletableFuture<BlockPos> completableFuture = new CompletableFuture<>();
        Future<?> future = locatingExecutorService.submit(() -> completableFuture.complete(
                level.findNearestMapStructure(structureTag, pos, searchRadius, skipKnownStructures)
        ));
        return new LocateTask<>(level.getServer(), completableFuture, future);
    }

    public static LocateTask<Pair<BlockPos, Holder<Structure>>> locate(ServerLevel level, HolderSet<Structure> structureSet, BlockPos pos, int searchRadius, boolean skipKnownStructures) {
        CompletableFuture<Pair<BlockPos, Holder<Structure>>> completableFuture = new CompletableFuture<>();
        Future<?> future = locatingExecutorService.submit(() -> completableFuture.complete(
                level.getChunkSource().getGenerator().findNearestMapStructure(level, structureSet, pos, searchRadius, skipKnownStructures)
        ));
        return new LocateTask<>(level.getServer(), completableFuture, future);
    }

    public record LocateTask<T>(MinecraftServer server, CompletableFuture<T> completableFuture, Future<?> taskFuture) {
        public LocateTask<T> then(Consumer<T> action) {
            completableFuture.thenAccept(action);
            return this;
        }

        public LocateTask<T> thenOnServerThread(Consumer<T> action) {
            completableFuture.thenAccept(result -> server.submit(() -> action.accept(result)));
            return this;
        }

        public void cancel() {
            taskFuture.cancel(true);
            completableFuture.cancel(false);
        }
    }
}
