package com.craisinlord.integrated_api.world.structures.pieces;

import com.craisinlord.integrated_api.modinit.IAStructurePieces;
import com.craisinlord.integrated_api.world.condition.StructureCondition;
import com.craisinlord.integrated_api.world.structures.modifier.StructureModifier;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptation;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Custom SinglePoolElement with support for many additional settings.
 */
public class IASinglePoolElement extends IAPoolElement {
    private static final Codec<Either<Identifier, StructureTemplate>> TEMPLATE_CODEC = Codec.of(IASinglePoolElement::encodeTemplate, Identifier.CODEC.map(Either::left));
    public static final MapCodec<IASinglePoolElement> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
            .group(
                    templateCodec(),
                    processorsCodec(),
                    projectionCodec(),
                    overrideLiquidSettingsCodec(),
                    nameCodec(),
                    maxCountCodec(),
                    minRequiredDepthCodec(),
                    maxPossibleDepthCodec(),
                    isPriorityCodec(),
                    ignoreBoundsCodec(),
                    conditionCodec(),
                    enhancedTerrainAdaptationCodec(),
                    Identifier.CODEC.optionalFieldOf("deadend_pool").forGetter(element -> element.deadendPool),
                    StructureModifier.CODEC.listOf().optionalFieldOf("modifiers", new ArrayList<>()).forGetter(element -> element.modifiers)
            ).apply(builder, IASinglePoolElement::new));

    public final Either<Identifier, StructureTemplate> template;

    public final Holder<StructureProcessorList> processors;

    public final Optional<LiquidSettings> overrideLiquidSettings;


    /**
     * Whether this piece should apply dead end adjustments.
     * If enabled, this piece has the possibility of being converted into one of its fallback pieces under certain conditions:
     * <ul>
     *     <li>This piece has one or more jigsaw blocks in addition to the one required to connect to its parent piece.</li>
     *     <li>This piece is not able to generate any other pieces from itself, including encased pieces.
     *     This could be due to either overlap with other pieces or reaching the max depth.</li>
     * </ul>
     * If both of these conditions are met, then a piece marked for deadend adjustments will be replaced with
     * an appropriate piece from its fallback pool instead.
     * <br />
     * <p>
     * <b>Note that at least one of the fallback pool elements MUST be a "true" terminator, i.e. have NO jigsaw pieces
     * except the one required for connection to its parent piece.</b> If all of your fallback pieces have more than one
     * jigsaw block, your structure's generation may get stuck in an infinite loop!
     * </p>
     */
    public final Optional<Identifier> deadendPool;

    /**
     * Post-placement modifiers.
     * These are modifications run on a piece after the entire structure's layout has been generated,
     * allowing for last-second conditional changes.
     */
    public final List<StructureModifier> modifiers;

    public IASinglePoolElement(
            Either<Identifier, StructureTemplate> template,
            Holder<StructureProcessorList> processors,
            StructureTemplatePool.Projection projection,
            Optional<LiquidSettings> overrideLiquidSettings,
            Optional<String> name,
            Optional<Integer> maxCount,
            Optional<Integer> minRequiredDepth,
            Optional<Integer> maxPossibleDepth,
            boolean isPriority,
            boolean ignoreBounds,
            StructureCondition condition,
            Optional<EnhancedTerrainAdaptation> enhancedTerrainAdaptation,
            Optional<Identifier> deadendPool,
            List<StructureModifier> modifiers
    ) {
        super(projection, name, maxCount, minRequiredDepth, maxPossibleDepth, isPriority, ignoreBounds, condition, enhancedTerrainAdaptation);
        this.template = template;
        this.processors = processors;
        this.overrideLiquidSettings = overrideLiquidSettings;
        this.deadendPool = deadendPool;
        this.modifiers = modifiers;
    }

    @Override
    public Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation) {
        StructureTemplate structureTemplate = this.getTemplate(structureTemplateManager);
        return structureTemplate.getSize(rotation);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
            StructureTemplateManager structureTemplateManager,
            BlockPos blockPos,
            Rotation rotation,
            RandomSource randomSource
    ) {
        StructureTemplate structureTemplate = this.getTemplate(structureTemplateManager);
        ObjectArrayList<StructureTemplate.StructureBlockInfo> jigsawBlocks = structureTemplate.filterBlocks(blockPos, (new StructurePlaceSettings()).setRotation(rotation), Blocks.JIGSAW, true);
        Util.shuffle(jigsawBlocks, randomSource);
        return jigsawBlocks;
    }

    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation) {
        StructureTemplate structureTemplate = this.getTemplate(structureTemplateManager);
        return structureTemplate.getBoundingBox((new StructurePlaceSettings()).setRotation(rotation), blockPos);
    }

    @Override
    public boolean place(StructureTemplateManager structureTemplateManager,
                         WorldGenLevel worldGenLevel,
                         StructureManager structureManager,
                         ChunkGenerator chunkGenerator,
                         BlockPos pos,
                         BlockPos pivotPos,
                         Rotation rotation,
                         BoundingBox boundingBox,
                         RandomSource randomSource,
                         LiquidSettings liquidSettings,
                         boolean replaceJigsaws
    ) {
        StructureTemplate structureTemplate = this.getTemplate(structureTemplateManager);
        StructurePlaceSettings structurePlaceSettings = this.getSettings(rotation, boundingBox, liquidSettings, replaceJigsaws);
        if (!structureTemplate.placeInWorld(worldGenLevel, pos, pivotPos, structurePlaceSettings, randomSource, 18)) {
            return false;
        } else {
            for (StructureTemplate.StructureBlockInfo structureBlockInfo : StructureTemplate.processBlockInfos(worldGenLevel, pos, pivotPos, structurePlaceSettings, this.getDataMarkers(structureTemplateManager, pos, rotation, false))) {
                this.handleDataMarker(worldGenLevel, structureBlockInfo, pos, rotation, randomSource, boundingBox);
            }

            return true;
        }
    }
    public Optional<Identifier> getDeadendPool() {
        return this.deadendPool;
    }

    public boolean hasModifiers() {
        return this.modifiers.size() > 0;
    }

    public StructureTemplate getTemplate(StructureTemplateManager structureTemplateManager) {
        return this.template.map(structureTemplateManager::getOrCreate, Function.identity());
    }

    public StructurePoolElementType<?> getType() {
        return IAStructurePieces.IA_POOL_ELEMENT.get();
    }

    public String toString() {
        return String.format("IntegratedAPIJigsawSingle[%s][%s][%s][%s]",
                this.name.orElse("<unnamed>"),
                this.template,
                this.maxCount.isPresent() ? maxCount.get() : "no max count",
                this.isPriority);
    }

    private StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, LiquidSettings liquidSettings, boolean replaceJigsaws) {
        StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings();
        structurePlaceSettings.setBoundingBox(boundingBox);
        structurePlaceSettings.setRotation(rotation);
        structurePlaceSettings.setKnownShape(true);
        structurePlaceSettings.setIgnoreEntities(false);
        structurePlaceSettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        structurePlaceSettings.setFinalizeEntities(true);
        structurePlaceSettings.setLiquidSettings(this.overrideLiquidSettings.orElse(liquidSettings));
        if (!replaceJigsaws) {
            structurePlaceSettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }

        this.processors.value().list().forEach(structurePlaceSettings::addProcessor);
        this.getProjection().getProcessors().forEach(structurePlaceSettings::addProcessor);
        return structurePlaceSettings;
    }

    private List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, boolean isPositionLocal) {
        StructureTemplate structureTemplate = this.getTemplate(structureTemplateManager);
        List<StructureTemplate.StructureBlockInfo> structureBlocks = structureTemplate.filterBlocks(blockPos, (new StructurePlaceSettings()).setRotation(rotation), Blocks.STRUCTURE_BLOCK, isPositionLocal);
        List<StructureTemplate.StructureBlockInfo> dataBlocks = Lists.newArrayList();

        for(StructureTemplate.StructureBlockInfo block : structureBlocks) {
            StructureMode structureMode = StructureMode.valueOf(block.nbt().getString("mode"));
            if (structureMode == StructureMode.DATA) {
                dataBlocks.add(block);
            }
        }

        return dataBlocks;
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, Holder<StructureProcessorList>> processorsCodec() {
        return StructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter(element -> element.processors);
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, Either<Identifier, StructureTemplate>> templateCodec() {
        return TEMPLATE_CODEC.fieldOf("location").forGetter(($$0) -> $$0.template);
    }

    public static <E extends IASinglePoolElement> RecordCodecBuilder<E, Optional<LiquidSettings>> overrideLiquidSettingsCodec() {
        return LiquidSettings.CODEC.optionalFieldOf("override_liquid_settings").forGetter(element -> element.overrideLiquidSettings);
    }

    private static <T> DataResult<T> encodeTemplate(Either<Identifier, StructureTemplate> either, DynamicOps<T> ops, T template) {
        Optional<Identifier> optional = either.left();
        return !optional.isPresent() ? DataResult.error(() -> "Can not serialize a runtime pool element") : Identifier.CODEC.encode(optional.get(), ops, template);
    }
}