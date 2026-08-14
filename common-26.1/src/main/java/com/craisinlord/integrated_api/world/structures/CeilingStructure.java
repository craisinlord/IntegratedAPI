package com.craisinlord.integrated_api.world.structures;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.IAStructures;
import com.craisinlord.integrated_api.utils.GeneralUtils;
import com.craisinlord.integrated_api.world.structures.codecs.YRangeAllowance;
import com.craisinlord.integrated_api.world.structures.pieces.manager.PieceLimitedJigsawManager;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptation;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptationType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class CeilingStructure extends JigsawStructure {

    public static final MapCodec<CeilingStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CeilingStructure.settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            Codec.intRange(0, 128).fieldOf("size").forGetter(structure -> structure.size),
            YRangeAllowance.CODEC.optionalFieldOf("y_allowance").forGetter(structure -> structure.yAllowance),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Codec.BOOL.fieldOf("cannot_spawn_in_liquid").orElse(false).forGetter(structure -> structure.cannotSpawnInLiquid),
            Codec.intRange(1, 100).optionalFieldOf("valid_biome_radius_check").forGetter(structure -> structure.biomeRadius),
            Codec.intRange(1, IntegratedAPI.NEW_STRUCTURE_SIZE).optionalFieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
            Codec.BOOL.fieldOf("rotation_fixed").orElse(false).forGetter(structure -> structure.rotationFixed),
            EnhancedTerrainAdaptationType.ADAPTATION_CODEC.optionalFieldOf("enhanced_terrain_adaptation", EnhancedTerrainAdaptation.NONE).forGetter(structure -> structure.enhancedTerrainAdaptation),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", net.minecraft.world.level.levelgen.structure.structures.JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings),
            Codec.intRange(1, 512).fieldOf("max_roof_search_distance").forGetter(structure -> structure.maxRoofSearchDistance),
            Codec.intRange(1, 64).fieldOf("min_roof_thickness").forGetter(structure -> structure.minRoofThickness),
            Codec.intRange(1, 512).fieldOf("min_open_space_below").forGetter(structure -> structure.minOpenSpaceBelow),
            Codec.intRange(0, 512).fieldOf("ceiling_offset").orElse(0).forGetter(structure -> structure.ceilingOffset)
    ).apply(instance, CeilingStructure::new));

    public final int maxRoofSearchDistance;
    public final int minRoofThickness;
    public final int minOpenSpaceBelow;
    public final int ceilingOffset;

    public CeilingStructure(StructureSettings config,
                            Holder<StructureTemplatePool> startPool,
                            int size,
                            Optional<YRangeAllowance> yAllowance,
                            HeightProvider startHeight,
                            boolean cannotSpawnInLiquid,
                            Optional<Integer> biomeRadius,
                            Optional<Integer> maxDistanceFromCenter,
                            boolean rotationFixed,
                            EnhancedTerrainAdaptation enhancedTerrainAdaptation,
                            LiquidSettings liquidSettings,
                            int maxRoofSearchDistance,
                            int minRoofThickness,
                            int minOpenSpaceBelow,
                            int ceilingOffset) {
        super(
                config,
                startPool,
                size,
                yAllowance,
                startHeight,
                Optional.empty(),
                cannotSpawnInLiquid,
                Optional.empty(),
                Optional.empty(),
                biomeRadius,
                maxDistanceFromCenter,
                Optional.empty(),
                rotationFixed,
                enhancedTerrainAdaptation,
                liquidSettings
        );
        this.maxRoofSearchDistance = maxRoofSearchDistance;
        this.minRoofThickness = minRoofThickness;
        this.minOpenSpaceBelow = minOpenSpaceBelow;
        this.ceilingOffset = ceilingOffset;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int offsetY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        ChunkPos chunkPos = context.chunkPos();
        int searchX = chunkPos.getMiddleBlockX();
        int searchZ = chunkPos.getMiddleBlockZ();
        Optional<CeilingAnchor> anchor = findCeilingAnchor(context, searchX, searchZ, offsetY);
        if (anchor.isEmpty()) {
            return Optional.empty();
        }

        BlockPos searchPos = new BlockPos(chunkPos.getMinBlockX(), anchor.get().roofY(), chunkPos.getMinBlockZ());
        if (!extraSpawningChecks(context, searchPos)) {
            return Optional.empty();
        }

        int topClipOff = Integer.MAX_VALUE;
        int bottomClipOff = Integer.MIN_VALUE;

        if (this.yAllowance.isPresent()) {
            if (this.yAllowance.get().maxYAllowed.isPresent()) {
                topClipOff = Math.min(topClipOff, this.yAllowance.get().maxYAllowed.get());
            }

            if (this.yAllowance.get().minYAllowed.isPresent()) {
                bottomClipOff = Math.max(bottomClipOff, this.yAllowance.get().minYAllowed.get());
            }
        }

        String rotationString = this.rotationFixed ? "NONE" : "RANDOM";
        int finalTopClipOff = topClipOff;
        int finalBottomClipOff = bottomClipOff;
        int roofY = anchor.get().roofY();
        BlockPos placementPos = new BlockPos(chunkPos.getMinBlockX(), roofY, chunkPos.getMinBlockZ());

        return PieceLimitedJigsawManager.assembleJigsawStructure(
                context,
                this.startPool,
                this.size,
                context.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(this),
                placementPos,
                false,
                Optional.empty(),
                topClipOff,
                bottomClipOff,
                null,
                this.maxDistanceFromCenter,
                rotationString,
                Optional.empty(),
                this.liquidSettings,
                (structurePiecesBuilder, pieces) -> postLayoutAdjustments(structurePiecesBuilder, placementPos, finalTopClipOff, finalBottomClipOff, roofY, pieces)
        );
    }

    protected void postLayoutAdjustments(StructurePiecesBuilder structurePiecesBuilder,
                                         BlockPos placementPos,
                                         int topClipOff,
                                         int bottomClipOff,
                                         int roofY,
                                         List<PoolElementStructurePiece> pieces) {
        GeneralUtils.centerAllPieces(placementPos, pieces);
        BoundingBox box = structurePiecesBuilder.getBoundingBox();
        int targetTopY = roofY - 1 - this.ceilingOffset;
        int yOffset = targetTopY - box.maxY();
        for (StructurePiece piece : pieces) {
            GeneralUtils.movePieceProperly(piece, 0, yOffset, 0);
        }

        BoundingBox adjustedBox = structurePiecesBuilder.getBoundingBox();
        if (adjustedBox.maxY() > roofY - 1) {
            pieces.clear();
            structurePiecesBuilder.clear();
            return;
        }

        if (topClipOff != Integer.MAX_VALUE && adjustedBox.maxY() > topClipOff) {
            pieces.clear();
            structurePiecesBuilder.clear();
            return;
        }

        if (bottomClipOff != Integer.MIN_VALUE && adjustedBox.minY() < bottomClipOff) {
            pieces.clear();
            structurePiecesBuilder.clear();
        }
    }

    private Optional<CeilingAnchor> findCeilingAnchor(GenerationContext context, int x, int z, int startY) {
        int minY = context.heightAccessor().getMinBuildHeight();
        int maxY = context.heightAccessor().getMaxBuildHeight() - 1;
        int lowerBound = Math.max(startY, minY + this.minOpenSpaceBelow);
        int upperBound = Math.min(startY + this.maxRoofSearchDistance, maxY - this.minRoofThickness + 1);
        if (lowerBound > upperBound) {
            return Optional.empty();
        }

        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
        for (int roofY = lowerBound; roofY <= upperBound; roofY++) {
            if (!isValidRoof(column, roofY)) {
                continue;
            }
            if (!hasOpenSpaceBelow(column, roofY)) {
                continue;
            }
            return Optional.of(new CeilingAnchor(roofY));
        }
        return Optional.empty();
    }

    private boolean isValidRoof(NoiseColumn column, int roofY) {
        for (int y = roofY; y < roofY + this.minRoofThickness; y++) {
            if (!isSolidForRoof(column.getBlock(y))) {
                return false;
            }
        }
        return true;
    }

    private boolean hasOpenSpaceBelow(NoiseColumn column, int roofY) {
        for (int y = roofY - 1; y >= roofY - this.minOpenSpaceBelow; y--) {
            if (!column.getBlock(y).isAir()) {
                return false;
            }
        }
        return true;
    }

    private boolean isSolidForRoof(net.minecraft.world.level.block.state.BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty() && state.canOcclude();
    }

    @Override
    public @NotNull BoundingBox adjustBoundingBox(@NotNull BoundingBox boundingBox) {
        return super.adjustBoundingBox(boundingBox).inflatedBy(this.enhancedTerrainAdaptation.getKernelRadius());
    }

    @Override
    public StructureType<?> type() {
        return IAStructures.CEILING_STRUCTURE.get();
    }

    private record CeilingAnchor(int roofY) { }
}
