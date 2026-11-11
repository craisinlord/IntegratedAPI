package com.craisinlord.integrated_api.modinit.registry.neoforge;

import com.craisinlord.integrated_api.modinit.registry.CustomRegistryLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;

public final class NeoForgeCustomRegistry<T, K extends T> implements CustomRegistryLookup<T, K> {
    private final Registry<T> registry;

    public NeoForgeCustomRegistry(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public boolean containsKey(Identifier id) {
        return registry.containsKey(id);
    }

    @Override
    public @Nullable T get(Identifier id) {
        return registry.getOptional(id).orElse(null);
    }

    @Override
    public Collection<T> getValues() {
        return registry.stream().toList();
    }

    @Override
    public Collection<Identifier> getKeys() {
        return registry.keySet();
    }

    @Override
    public @Nullable Identifier getKey(T value) {
        return registry.getKey(value);
    }

    @Override
    public boolean containsValue(T value) {
        return registry.getKey(value) != null;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return registry.iterator();
    }
}
