package com.craisinlord.integrated_api.world.condition;

import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public class AnyOfCondition extends StructureCondition {
    public static final MapCodec<AnyOfCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            StructureConditionType.CONDITION_CODEC.listOf().fieldOf("conditions").forGetter(condition -> condition.conditions)
    ).apply(builder, AnyOfCondition::new));

    private final List<StructureCondition> conditions;

    public AnyOfCondition(List<StructureCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public StructureConditionType<?> type() {
        return StructureConditionType.ANY_OF;
    }

    @Override
    public boolean passes(StructureContext ctx) {
        return conditions.stream().anyMatch(condition -> condition.passes(ctx));
    }
}
