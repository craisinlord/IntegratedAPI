package com.craisinlord.integrated_api.world.condition;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public class RandomChanceCondition extends StructureCondition {
    public static final MapCodec<RandomChanceCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("chance").forGetter(condition -> condition.chance)
    ).apply(builder, RandomChanceCondition::new));

    public final float chance;

    public RandomChanceCondition(float chance) {
        this.chance = chance;
    }

    @Override
    public StructureConditionType<?> type() {
        return StructureConditionType.RANDOM_CHANCE;
    }

    @Override
    public boolean passes(StructureContext ctx) {
        RandomSource random = ctx.random();
        if (random == null) {
            IntegratedAPI.LOGGER.error("Missing required field 'random' for random_chance condition!");
            return false;
        }
        return random.nextFloat() < this.chance;
    }
}
