package com.craisinlord.integrated_api.modinit.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;
import java.util.function.Supplier;

public final class ResourcefulRegistries {

    private static RegistryFactory registryFactory;
    private static CustomRegistryFactory customRegistryFactory;

    private ResourcefulRegistries() {
    }

    public static void init(RegistryFactory registryFactory, CustomRegistryFactory customRegistryFactory) {
        ResourcefulRegistries.registryFactory = Objects.requireNonNull(registryFactory);
        ResourcefulRegistries.customRegistryFactory = Objects.requireNonNull(customRegistryFactory);
    }

    public static <T> ResourcefulRegistry<T> create(ResourcefulRegistry<T> parent) {
        return new ResourcefulRegistryChild<>(parent);
    }

    public static <T> ResourcefulRegistry<T> create(Registry<T> registry, String id) {
        if (registryFactory == null) {
            throw new IllegalStateException("ResourcefulRegistries not initialized");
        }
        return registryFactory.create(registry, id);
    }

    public static <T, K extends Registry<T>> Pair<Supplier<CustomRegistryLookup<T, T>>, ResourcefulRegistry<T>> createCustomRegistryInternal(String modId, ResourceKey<K> key, boolean save, boolean sync, boolean allowModification) {
        if (customRegistryFactory == null) {
            throw new IllegalStateException("ResourcefulRegistries not initialized");
        }
        return customRegistryFactory.create(modId, key, save, sync, allowModification);
    }

    @FunctionalInterface
    public interface RegistryFactory {
        <T> ResourcefulRegistry<T> create(Registry<T> registry, String id);
    }

    @FunctionalInterface
    public interface CustomRegistryFactory {
        <T, K extends Registry<T>> Pair<Supplier<CustomRegistryLookup<T, T>>, ResourcefulRegistry<T>> create(ResourceKey<K> key, String modId, boolean save, boolean sync, boolean allowModification);
    }
}
