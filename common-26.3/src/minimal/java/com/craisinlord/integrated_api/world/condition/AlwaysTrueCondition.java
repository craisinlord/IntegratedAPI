package com.craisinlord.integrated_api.world.condition;

import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.mojang.serialization.MapCodec;

public class AlwaysTrueCondition extends StructureCondition {
    private static final AlwaysTrueCondition INSTANCE = new AlwaysTrueCondition();
    public static final MapCodec<AlwaysTrueCondition> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    public StructureConditionType<?> type() {
        return StructureConditionType.ALWAYS_TRUE;
    }

    @Override
    public boolean passes(StructureContext ctx) {
        return true;
    }
}
