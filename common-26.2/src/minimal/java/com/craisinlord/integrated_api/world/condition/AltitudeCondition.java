package com.craisinlord.integrated_api.world.condition;

import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public class AltitudeCondition extends StructureCondition {
    public static final MapCodec<AltitudeCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.DOUBLE.optionalFieldOf("bottom_cutoff_y").forGetter(condition -> condition.bottomCutoffY),
            Codec.DOUBLE.optionalFieldOf("top_cutoff_y").forGetter(condition -> condition.topCutoffY)
    ).apply(builder, AltitudeCondition::new));

    private final Optional<Double> bottomCutoffY;
    private final Optional<Double> topCutoffY;

    public AltitudeCondition(Optional<Double> bottomCutoffY, Optional<Double> topCutoffY) {
        this.bottomCutoffY = bottomCutoffY;
        this.topCutoffY = topCutoffY;
    }

    @Override
    public StructureConditionType<?> type() {
        return StructureConditionType.ALTITUDE;
    }

    @Override
    public boolean passes(StructureContext ctx) {
        if (bottomCutoffY.isPresent() && ctx.pieceMinY() < bottomCutoffY.get()) {
            return false;
        }
        if (topCutoffY.isPresent() && ctx.pieceMaxY() > topCutoffY.get()) {
            return false;
        }
        return true;
    }
}
