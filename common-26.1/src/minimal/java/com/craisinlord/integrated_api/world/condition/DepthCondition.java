package com.craisinlord.integrated_api.world.condition;

import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;

public class DepthCondition extends StructureCondition {
    public static final MapCodec<DepthCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("min_required_depth").forGetter(condition -> condition.minRequiredDepth),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("max_possible_depth").forGetter(condition -> condition.maxPossibleDepth)
    ).apply(builder, DepthCondition::new));

    public final Optional<Integer> minRequiredDepth;
    public final Optional<Integer> maxPossibleDepth;

    public DepthCondition(Optional<Integer> minRequiredDepth, Optional<Integer> maxPossibleDepth) {
        this.minRequiredDepth = minRequiredDepth;
        this.maxPossibleDepth = maxPossibleDepth;
    }

    @Override
    public StructureConditionType<?> type() {
        return StructureConditionType.DEPTH;
    }

    @Override
    public boolean passes(StructureContext ctx) {
        int depth = ctx.depth();
        boolean isAtMinRequiredDepth = minRequiredDepth.isEmpty() || minRequiredDepth.get() <= depth;
        boolean isAtMaxAllowableDepth = maxPossibleDepth.isEmpty() || maxPossibleDepth.get() >= depth;
        return isAtMinRequiredDepth && isAtMaxAllowableDepth;
    }
}
