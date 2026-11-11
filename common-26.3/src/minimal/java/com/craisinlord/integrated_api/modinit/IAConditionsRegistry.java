package com.craisinlord.integrated_api.modinit;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.registry.CustomRegistry;
import com.craisinlord.integrated_api.modinit.registry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public final class IAConditionsRegistry {
    private IAConditionsRegistry() {
    }

    public static final ResourceKey<Registry<Supplier<Boolean>>> IA_JSON_CONDITIONS_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(IntegratedAPI.MODID, "json_conditions"));
    public static final CustomRegistry<Supplier<Boolean>> IA_JSON_CONDITIONS_REGISTRY =
            CustomRegistry.of(IntegratedAPI.MODID, IA_JSON_CONDITIONS_KEY, false, false, true);
    public static final RegistryEntry<Supplier<Boolean>> ALWAYS_TRUE =
            IA_JSON_CONDITIONS_REGISTRY.register("always_true", () -> () -> true);
    public static final RegistryEntry<Supplier<Boolean>> ALWAYS_FALSE =
            IA_JSON_CONDITIONS_REGISTRY.register("always_false", () -> () -> false);
}
