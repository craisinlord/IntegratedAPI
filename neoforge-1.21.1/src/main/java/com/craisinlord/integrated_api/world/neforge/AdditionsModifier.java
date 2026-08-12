package com.craisinlord.integrated_api.world.neforge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.craisinlord.integrated_api.modinit.neoforge.IABiomeModifiers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;


public record AdditionsModifier(HolderSet<Biome> biomes, Holder<PlacedFeature> feature, GenerationStep.Decoration step) implements net.neoforged.neoforge.common.world.BiomeModifier {

    public static MapCodec<AdditionsModifier> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(AdditionsModifier::biomes),
            PlacedFeature.CODEC.fieldOf("feature").forGetter(AdditionsModifier::feature),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AdditionsModifier::step)
    ).apply(builder, AdditionsModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, net.neoforged.neoforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        // add a feature to all specified biomes
        if (phase == Phase.ADD && biomes.contains(biome)) {
            builder.getGenerationSettings().addFeature(step, feature);
        }
    }

    public MapCodec<? extends net.neoforged.neoforge.common.world.BiomeModifier> codec() {
        return IABiomeModifiers.ADDITIONS_MODIFIER.get();
    }
}