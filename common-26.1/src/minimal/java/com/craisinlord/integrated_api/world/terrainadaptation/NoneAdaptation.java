package com.craisinlord.integrated_api.world.terrainadaptation;

import com.mojang.serialization.MapCodec;

public class NoneAdaptation extends EnhancedTerrainAdaptation {
    private static final NoneAdaptation INSTANCE = new NoneAdaptation();
    public static final MapCodec<NoneAdaptation> CODEC = MapCodec.unit(() -> INSTANCE);

    public NoneAdaptation() {
        super(0, 0, false, false);
    }

    @Override
    public EnhancedTerrainAdaptationType<?> type() {
        return EnhancedTerrainAdaptationType.NONE;
    }

    @Override
    public double computeDensityFactor(int xDistance, int yDistance, int zDistance, int yDistanceToBeardBase) {
        return 0;
    }
}
