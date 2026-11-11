package com.craisinlord.integrated_api.modinit.neoforge;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.world.neforge.AdditionsModifier;
import com.craisinlord.integrated_api.world.neforge.RemovalsModifier;
import com.craisinlord.integrated_api.world.neforge.AdditionsTemperatureModifier;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class IABiomeModifiers {
	public static final net.neoforged.neoforge.registries.DeferredRegister<MapCodec<? extends net.neoforged.neoforge.common.world.BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = net.neoforged.neoforge.registries.DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, IntegratedAPI.MODID);

	public static final DeferredHolder<MapCodec<? extends net.neoforged.neoforge.common.world.BiomeModifier>, MapCodec<AdditionsModifier>> ADDITIONS_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("additions_modifier", () -> AdditionsModifier.CODEC);
	public static final DeferredHolder<MapCodec<? extends net.neoforged.neoforge.common.world.BiomeModifier>, MapCodec<AdditionsTemperatureModifier>> ADDITIONS_TEMPERATURE_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("additions_temperature_modifier", () -> AdditionsTemperatureModifier.CODEC);
	public static final DeferredHolder<MapCodec<? extends net.neoforged.neoforge.common.world.BiomeModifier>, MapCodec<RemovalsModifier>> REMOVALS_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("removals_modifier", () -> RemovalsModifier.CODEC);
}