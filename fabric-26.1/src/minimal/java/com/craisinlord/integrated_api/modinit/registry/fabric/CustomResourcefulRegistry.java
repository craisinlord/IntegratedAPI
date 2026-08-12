package com.craisinlord.integrated_api.modinit.registry.fabric;

import com.craisinlord.integrated_api.modinit.registry.BasicRegistryEntry;
import com.craisinlord.integrated_api.modinit.registry.RegistryEntries;
import com.craisinlord.integrated_api.modinit.registry.RegistryEntry;
import com.craisinlord.integrated_api.modinit.registry.ResourcefulRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.function.Supplier;

public final class CustomResourcefulRegistry<T> implements ResourcefulRegistry<T> {
    private final RegistryEntries<T> entries = new RegistryEntries<>();
    private final Registry<T> registry;
    private final String id;

    public CustomResourcefulRegistry(Registry<T> registry, String id) {
        this.registry = registry;
        this.id = id;
    }

    @Override
    public <I extends T> RegistryEntry<I> register(String id, Supplier<I> supplier) {
        Identifier identifier = Identifier.fromNamespaceAndPath(this.id, id);
        I value = Registry.register(registry, identifier, supplier.get());
        return entries.add(new BasicRegistryEntry<>(identifier, value));
    }

    @Override
    public Collection<RegistryEntry<T>> getEntries() {
        return entries.getEntries();
    }

    @Override
    public void init() {
    }
}
