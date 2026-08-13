package com.craisinlord.integrated_api.world.terrainadaptation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public class CustomAdaptation extends EnhancedTerrainAdaptation {
    public static final Codec<CustomAdaptation> CODEC = RecordCodecBuilder.create((builder) -> builder
            .group(
                    Codec.BOOL.optionalFieldOf("carves", true).forGetter(EnhancedTerrainAdaptation::carves),
                    Codec.BOOL.optionalFieldOf("beards", true).forGetter(EnhancedTerrainAdaptation::beards),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("kernel_size").forGetter(EnhancedTerrainAdaptation::getKernelSize),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("kernel_distance").forGetter(EnhancedTerrainAdaptation::getKernelDistance),
                    TerrainAdaptationDirection.CODEC.optionalFieldOf("direction", TerrainAdaptationDirection.DOWN).forGetter(EnhancedTerrainAdaptation::direction))
            .apply(builder, CustomAdaptation::new));

    CustomAdaptation(boolean doCarving, boolean doBearding, int kernelSize, int kernelDistance, TerrainAdaptationDirection direction) {
        super(kernelSize, kernelDistance, doCarving, doBearding, direction);
    }

    @Override
    public EnhancedTerrainAdaptationType<?> type() {
        return EnhancedTerrainAdaptationType.CUSTOM;
    }
}
