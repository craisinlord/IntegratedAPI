package com.craisinlord.integrated_api.world.condition;

import com.craisinlord.integrated_api.utils.PlatformHooks;
import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ModLoadedCondition extends StructureCondition {
    public static final MapCodec<ModLoadedCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("modid").forGetter(condition -> condition.modId)
    ).apply(builder, ModLoadedCondition::new));

    private final String modId;

    public ModLoadedCondition(String modId) {
        this.modId = modId;
    }

    @Override
    public StructureConditionType<?> type() {
        return StructureConditionType.MOD_LOADED;
    }

    @Override
    public boolean passes(StructureContext ctx) {
        return PlatformHooks.isModLoaded(this.modId);
    }
}
