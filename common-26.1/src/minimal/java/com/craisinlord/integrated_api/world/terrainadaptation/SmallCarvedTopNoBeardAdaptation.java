package com.craisinlord.integrated_api.world.terrainadaptation;

import com.mojang.serialization.MapCodec;

public class SmallCarvedTopNoBeardAdaptation extends EnhancedTerrainAdaptation {
    private static final SmallCarvedTopNoBeardAdaptation INSTANCE = new SmallCarvedTopNoBeardAdaptation();
    public static final MapCodec<SmallCarvedTopNoBeardAdaptation> CODEC = MapCodec.unit(() -> INSTANCE);

    public SmallCarvedTopNoBeardAdaptation() {
        super(12, 6, true, false);
    }

    @Override
    public EnhancedTerrainAdaptationType<?> type() {
        return EnhancedTerrainAdaptationType.SMALL_CARVED_TOP_NO_BEARD;
    }
}
