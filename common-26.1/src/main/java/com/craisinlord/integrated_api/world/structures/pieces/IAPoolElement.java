package com.craisinlord.integrated_api.world.structures.pieces;

import com.craisinlord.integrated_api.world.condition.StructureCondition;
import com.craisinlord.integrated_api.world.condition.StructureConditionType;
import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptation;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptationType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.Optional;

public abstract class IAPoolElement extends StructurePoolElement {
    /**
     * The name of this piece.
     * Solely used to uniquely identify pieces for the sake of tracking max_count.
     */
    public final Optional<String> name;

    /**
     * The maximum possible number of pieces with this name.
     * If this setting is used, the 'name' MUST be specified.
     */
    public final Optional<Integer> maxCount;

    /**
     * The minimum required depth (in jigsaw pieces) from the structure start
     * at which this piece can spawn.
     *
     * @deprecated Use { DepthCondition} instead.
     */
    @Deprecated
    public final Optional<Integer> minRequiredDepth;

    /**
     * The maximum allowed depth (in jigsaw pieces) from the structure start
     * at which this piece will no longer spawn.
     *
     * @deprecated Use DepthCondition instead.
     */
    @Deprecated
    public final Optional<Integer> maxPossibleDepth;

    /**
     * Whether this is a priority piece.
     * Priority pieces attempt to generate before all other pieces in the pool,
     * regardless of weight.
     */
    public final boolean isPriority;

    /**
     * Whether this piece should ignore the usual piece boundary checks.
     * Enabling this allows this piece to spawn while overlapping other pieces.
     */
    public final boolean ignoreBounds;

    /**
     * Optional condition required for this piece to spawn.
     * Can be any { StructureCondition}, including compound conditions
     * ({ AnyOfCondition}, { AllOfCondition})
     */
    public final StructureCondition condition;

    /**
     * Optional enhanced terrain adaptation specific to this piece.
     * Takes priority over the structure's enhanced terrain adaptation if specified.
     */
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

    /* GETTERS */

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

    /* HELPERS */

    public boolean isAtValidDepth(int depth) {
        boolean isAtMinRequiredDepth = minRequiredDepth.isEmpty() || minRequiredDepth.get() <= depth;
        boolean isAtMaxAllowableDepth = maxPossibleDepth.isEmpty() || maxPossibleDepth.get() >= depth;
        return isAtMinRequiredDepth && isAtMaxAllowableDepth;
    }

    public boolean passesConditions(StructureContext ctx) {
        return this.condition.passes(ctx);
    }

    /* CODECS */

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