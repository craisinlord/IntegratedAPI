package com.craisinlord.integrated_api.world.structures.pieces;

import com.craisinlord.integrated_api.world.condition.StructureCondition;
import com.craisinlord.integrated_api.world.condition.StructureConditionType;
import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptation;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptationType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public abstract class IAPoolElement extends StructurePoolElement {
    public final Optional<String> name;
    public final Optional<Integer> maxCount;
    @Deprecated
    public final Optional<Integer> minRequiredDepth;
    @Deprecated
    public final Optional<Integer> maxPossibleDepth;
    public final boolean isPriority;
    public final boolean ignoreBounds;
    public final StructureCondition condition;
    public final Optional<EnhancedTerrainAdaptation> enhancedTerrainAdaptation;

    public IAPoolElement(
            StructureTemplatePool.Projection projection,
            Optional<String> name,
            Optional<Integer> maxCount,
            Optional<Integer> minRequiredDepth,
            Optional<Integer> maxPossibleDepth,
            boolean isPriority,
            boolean ignoreBounds,
            StructureCondition condition,
            Optional<EnhancedTerrainAdaptation> enhancedTerrainAdaptation
    ) {
        super(projection);
        this.name = name;
        this.maxCount = maxCount;
        this.minRequiredDepth = minRequiredDepth;
        this.maxPossibleDepth = maxPossibleDepth;
        this.isPriority = isPriority;
        this.ignoreBounds = ignoreBounds;
        this.condition = condition;
        this.enhancedTerrainAdaptation = enhancedTerrainAdaptation;
    }

    public Optional<String> getName() {
        return name;
    }

    public Optional<Integer> getMaxCount() {
        return maxCount;
    }

    @Deprecated
    public Optional<Integer> getMinRequiredDepth() {
        return minRequiredDepth;
    }

    @Deprecated
    public Optional<Integer> getMaxPossibleDepth() {
        return maxPossibleDepth;
    }

    public boolean isPriorityPiece() {
        return isPriority;
    }

    public boolean ignoresBounds() {
        return ignoreBounds;
    }

    public StructureCondition getCondition() {
        return condition;
    }

    public Optional<EnhancedTerrainAdaptation> getEnhancedTerrainAdaptation() {
        return enhancedTerrainAdaptation;
    }

    public boolean isAtValidDepth(int depth) {
        boolean isAtMinRequiredDepth = minRequiredDepth.isEmpty() || minRequiredDepth.get() <= depth;
        boolean isAtMaxAllowableDepth = maxPossibleDepth.isEmpty() || maxPossibleDepth.get() >= depth;
        return isAtMinRequiredDepth && isAtMaxAllowableDepth;
    }

    public boolean passesConditions(StructureContext ctx) {
        return this.condition.passes(ctx);
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, Optional<String>> nameCodec() {
        return Codec.STRING.optionalFieldOf("name").forGetter(IASinglePoolElement::getName);
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, Optional<Integer>> maxCountCodec() {
        return ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("max_count").forGetter(IASinglePoolElement::getMaxCount);
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, Optional<Integer>> minRequiredDepthCodec() {
        return ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("min_required_depth").forGetter(IASinglePoolElement::getMinRequiredDepth);
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, Optional<Integer>> maxPossibleDepthCodec() {
        return ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("max_possible_depth").forGetter(IASinglePoolElement::getMaxPossibleDepth);
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, Boolean> isPriorityCodec() {
        return Codec.BOOL.optionalFieldOf("is_priority", false).forGetter(IASinglePoolElement::isPriorityPiece);
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, Boolean> ignoreBoundsCodec() {
        return Codec.BOOL.optionalFieldOf("ignore_bounds", false).forGetter(IASinglePoolElement::ignoresBounds);
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, StructureCondition> conditionCodec() {
        return StructureConditionType.CONDITION_CODEC.optionalFieldOf("condition", StructureCondition.ALWAYS_TRUE).forGetter(IASinglePoolElement::getCondition);
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, Optional<EnhancedTerrainAdaptation>> enhancedTerrainAdaptationCodec() {
        return EnhancedTerrainAdaptationType.ADAPTATION_CODEC.optionalFieldOf("enhanced_terrain_adaptation").forGetter(IASinglePoolElement::getEnhancedTerrainAdaptation);
    }
}
