package com.craisinlord.integrated_api.world.structures;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.IAStructures;
import com.craisinlord.integrated_api.world.structures.codecs.YRangeAllowance;
import com.craisinlord.integrated_api.world.structures.pieces.manager.PieceLimitedJigsawManager;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptation;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptationType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class BiomeFacingStructure extends JigsawStructure {

    public static final MapCodec<BiomeFacingStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeFacingStructure.settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
            YRangeAllowance.CODEC.optionalFieldOf("y_allowance").forGetter(structure -> structure.yAllowance),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
            Codec.BOOL.fieldOf("cannot_spawn_in_liquid").orElse(false).forGetter(structure -> structure.cannotSpawnInLiquid),
            Codec.intRange(1, 100).optionalFieldOf("terrain_height_radius_check").forGetter(structure -> structure.terrainHeightCheckRadius),
            Codec.intRange(1, 1000).optionalFieldOf("allowed_terrain_height_range").forGetter(structure -> structure.allowedTerrainHeightRange),
            Codec.intRange(1, 100).optionalFieldOf("valid_biome_radius_check").forGetter(structure -> structure.biomeRadius),
            Codec.intRange(1, IntegratedAPI.NEW_STRUCTURE_SIZE).optionalFieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
            EnhancedTerrainAdaptationType.ADAPTATION_CODEC.optionalFieldOf("enhanced_terrain_adaptation", EnhancedTerrainAdaptation.NONE).forGetter(structure -> structure.enhancedTerrainAdaptation),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", net.minecraft.world.level.levelgen.structure.structures.JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings),
            Codec.intRange(1, 100).fieldOf("target_biome_radius_check_blocks").orElse(24).forGetter(structure -> structure.targetBiomeRadius),
            RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("target_biomes").forGetter(structure -> structure.targetBiomes)
    ).apply(instance, BiomeFacingStructure::new));

    private final int targetBiomeRadius;
    private final HolderSet<Biome> targetBiomes;

    public BiomeFacingStructure(StructureSettings config,
                                Holder<StructureTemplatePool> startPool,
                                int size,
                                Optional<YRangeAllowance> yAllowance,
                                HeightProvider startHeight,
                                Optional<Heightmap.Types> projectStartToHeightmap,
                                boolean cannotSpawnInLiquid,
                                Optional<Integer> terrainHeightCheckRadius,
                                Optional<Integer> allowedTerrainHeightRange,
                                Optional<Integer> biomeRadius,
                                Optional<Integer> maxDistanceFromCenter,
                                EnhancedTerrainAdaptation enhancedTerrainAdaptation,
                                LiquidSettings liquidSettings,
                                int targetBiomeRadius,
                                HolderSet<Biome> targetBiomes)
    {
        super(config,
                startPool,
                size,
                yAllowance,
                startHeight,
                projectStartToHeightmap,
                cannotSpawnInLiquid,
                terrainHeightCheckRadius,
                allowedTerrainHeightRange,
                biomeRadius,
                maxDistanceFromCenter,
                Optional.empty(),
                false,
                enhancedTerrainAdaptation,
                liquidSettings);
        this.targetBiomeRadius = targetBiomeRadius;
        this.targetBiomes = targetBiomes;
    }

    private String rotationToBiome(BlockPos blockpos, Structure.GenerationContext context) {
        int validBiomeRange = this.targetBiomeRadius;
        int sectionY = blockpos.getY();
        if (projectStartToHeightmap.isPresent()) {
            sectionY += context.chunkGenerator().getFirstOccupiedHeight(blockpos.getX(), blockpos.getZ(), projectStartToHeightmap.get(), context.heightAccessor(), context.randomState());
        }
        sectionY = QuartPos.fromBlock(sectionY);
        int posZ = blockpos.getZ();
        int posX = blockpos.getX();

        int sectionBiomeRange = (int) Math.ceil(validBiomeRange/4) + 2;

        Set<Holder<Biome>> section2Biomes = context.biomeSource().getBiomesWithin(posX - sectionBiomeRange, sectionY, posZ - (2*sectionBiomeRange), sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section2Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 2: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX - (sectionBiomeRange)) + " " + sectionY + " " + (posZ - (2*sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "NONE";
            }
        }
        Set<Holder<Biome>> section3Biomes = context.biomeSource().getBiomesWithin(posX + sectionBiomeRange, sectionY, posZ - (2*sectionBiomeRange), sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section3Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 3: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX + (sectionBiomeRange)) + " " + sectionY + " " + (posZ - (2*sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "NONE";
            }
        }
        Set<Holder<Biome>> section12Biomes = context.biomeSource().getBiomesWithin(posX + (2*sectionBiomeRange), sectionY, posZ + sectionBiomeRange, sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section12Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 12: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX + (2*sectionBiomeRange)) + " " + sectionY + " " + (posZ + (sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "CLOCKWISE_90";
            }
        }
        Set<Holder<Biome>> section8Biomes = context.biomeSource().getBiomesWithin(posX + (2*sectionBiomeRange), sectionY, posZ - sectionBiomeRange, sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section8Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 8: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX + (2*sectionBiomeRange)) + " " + sectionY + " " + (posZ - (sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "CLOCKWISE_90";
            }
        }
        Set<Holder<Biome>> section14Biomes = context.biomeSource().getBiomesWithin(posX - sectionBiomeRange, sectionY, posZ + (2*sectionBiomeRange), sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section14Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 14: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX - (sectionBiomeRange)) + " " + sectionY + " " + (posZ + (2*sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "CLOCKWISE_180";
            }
        }
        Set<Holder<Biome>> section15Biomes = context.biomeSource().getBiomesWithin(posX + sectionBiomeRange, sectionY, posZ + (2*sectionBiomeRange), sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section15Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 15: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX + (sectionBiomeRange)) + " " + sectionY + " " + (posZ + (2*sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "CLOCKWISE_180";
            }
        }
        Set<Holder<Biome>> section5Biomes = context.biomeSource().getBiomesWithin(posX - (2*sectionBiomeRange), sectionY, posZ - sectionBiomeRange, sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section5Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 5: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX - (2*sectionBiomeRange)) + " " + sectionY + " " + (posZ - (sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "COUNTERCLOCKWISE_90";
            }
        }
        Set<Holder<Biome>> section9Biomes = context.biomeSource().getBiomesWithin(posX - (2*sectionBiomeRange), sectionY, posZ + sectionBiomeRange, sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section9Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 9: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX - (2*sectionBiomeRange)) + " " + sectionY + " " + (posZ + (sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "COUNTERCLOCKWISE_90";
            }
        }
        Set<Holder<Biome>> section6Biomes = context.biomeSource().getBiomesWithin(posX - sectionBiomeRange, sectionY, posZ - sectionBiomeRange, sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section6Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 6: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX - (sectionBiomeRange)) + " " + sectionY + " " + (posZ - (sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "COUNTERCLOCKWISE_90";
            }
        }
        Set<Holder<Biome>> section7Biomes =context.biomeSource().getBiomesWithin(posX + sectionBiomeRange, sectionY, posZ - sectionBiomeRange, sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section7Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 7: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX + (sectionBiomeRange)) + " " + sectionY + " " + (posZ - (sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "CLOCKWISE_90";
            }
        }
        Set<Holder<Biome>> section11Biomes = context.biomeSource().getBiomesWithin(posX + sectionBiomeRange, sectionY, posZ + sectionBiomeRange, sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section11Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 11: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX + (sectionBiomeRange)) + " " + sectionY + " " + (posZ + (sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "CLOCKWISE_90";
            }
        }


        Set<Holder<Biome>> section10Biomes = context.biomeSource().getBiomesWithin(posX - sectionBiomeRange, sectionY, posZ + sectionBiomeRange, sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section10Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 10: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX - (sectionBiomeRange)) + " " + sectionY + " " + (posZ + (sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "COUNTERCLOCKWISE_90";
            }
        }
        Set<Holder<Biome>> section1Biomes = context.biomeSource().getBiomesWithin(posX - (2*sectionBiomeRange), sectionY, posZ - (2*sectionBiomeRange), sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section1Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 1: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX - (2*sectionBiomeRange)) + " " + sectionY + " " + (posZ - (2*sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "NONE";
            }
        }
        Set<Holder<Biome>> section4Biomes = context.biomeSource().getBiomesWithin(posX + (2*sectionBiomeRange), sectionY, posZ - (2*sectionBiomeRange), sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section4Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 4: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX + (2*sectionBiomeRange)) + " " + sectionY + " " + (posZ - (2*sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "NONE";
            }
        }
        Set<Holder<Biome>> section16Biomes = context.biomeSource().getBiomesWithin(posX + (2*sectionBiomeRange), sectionY, posZ + (2*sectionBiomeRange), sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section16Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 16: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX + (2*sectionBiomeRange)) + " " + sectionY + " " + (posZ + (2*sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "CLOCKWISE_180";
            }
        }
        Set<Holder<Biome>> section13Biomes = context.biomeSource().getBiomesWithin(posX - (2*sectionBiomeRange), sectionY, posZ + (2*sectionBiomeRange), sectionBiomeRange, context.randomState().sampler());
        for (Holder<Biome> biome : section13Biomes) {
//            IntegratedAPI.LOGGER.info("BIOME IN 13: " + biome);
//            IntegratedAPI.LOGGER.info("CHECKED: " + (posX - (2*sectionBiomeRange)) + " " + sectionY + " " + (posZ + (2*sectionBiomeRange)) + " " + sectionBiomeRange);
            if (this.targetBiomes.contains(biome)) {
                return "CLOCKWISE_180";
            }
        }
        IntegratedAPI.LOGGER.info("EVIL DETECTED YOU WILL BE EXTERMINATED");
        return "NONE";
    }


    private boolean checkBiome(BlockPos blockpos, Structure.GenerationContext context) {
        int validBiomeRange = this.targetBiomeRadius - 4;
        int sectionY = blockpos.getY();
        if (projectStartToHeightmap.isPresent()) {
            sectionY += context.chunkGenerator().getFirstOccupiedHeight(blockpos.getX(), blockpos.getZ(), projectStartToHeightmap.get(), context.heightAccessor(), context.randomState());
        }
        sectionY = QuartPos.fromBlock(sectionY);
        int posZ = blockpos.getZ();
        int posX = blockpos.getX();
        for (Holder<Biome> biome: context.biomeSource().getBiomesWithin(posX, sectionY, posZ, validBiomeRange, context.randomState().sampler())) {
            if (this.targetBiomes.contains(biome)) {
                return true;
            }
        }
        return false;
    }

    protected boolean extraSpawningChecks(GenerationContext context, BlockPos blockPos) {
        boolean superCheck = super.extraSpawningChecks(context, blockPos);
        if(!superCheck)
            return false;
        ChunkPos chunkPos = context.chunkPos();

        if (this.biomeRadius.isPresent() && !(context.biomeSource() instanceof CheckerboardColumnBiomeSource)) {
            int validBiomeRange = this.biomeRadius.get();
            int sectionY = blockPos.getY();
            if (projectStartToHeightmap.isPresent()) {
                sectionY += context.chunkGenerator().getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(), projectStartToHeightmap.get(), context.heightAccessor(), context.randomState());
            }
            sectionY = QuartPos.fromBlock(sectionY);

            for (int curChunkX = chunkPos.x - validBiomeRange; curChunkX <= chunkPos.x + validBiomeRange; curChunkX++) {
                for (int curChunkZ = chunkPos.z - validBiomeRange; curChunkZ <= chunkPos.z + validBiomeRange; curChunkZ++) {
                    Holder<Biome> biome = context.biomeSource().getNoiseBiome(QuartPos.fromSection(curChunkX), sectionY, QuartPos.fromSection(curChunkZ), context.randomState().sampler());
                    if (!context.validBiome().test(biome)) {
                        return false;
                    }
                }
            }
        }
        if (!checkBiome(blockPos, context)) {
            return false;
        }
        return true;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        int offsetY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

        BlockPos blockpos = new BlockPos(context.chunkPos().getMinBlockX(), offsetY, context.chunkPos().getMinBlockZ());

        if (!extraSpawningChecks(context, blockpos)) {
            return Optional.empty();
        }

        int topClipOff = Integer.MAX_VALUE;
        int bottomClipOff = Integer.MIN_VALUE;
        if (this.yAllowance.isPresent()) {
            if(this.yAllowance.get().maxYAllowed.isPresent()) {
                topClipOff = Math.min(topClipOff, this.yAllowance.get().maxYAllowed.get());
            }

            if(this.yAllowance.get().minYAllowed.isPresent()) {
                bottomClipOff = Math.max(bottomClipOff, this.yAllowance.get().minYAllowed.get());
            }
        }

        String rotationString = rotationToBiome(blockpos, context);

        if (rotationString==null) {
            throw new RuntimeException(new Exception("Integrated API Found Null Rotation for Biome Facing Structure. REPORT THIS TO CRAISIN!"));
        }

        int finalTopClipOff = topClipOff;
        int finalBottomClipOff = bottomClipOff;
        return PieceLimitedJigsawManager.assembleJigsawStructure(
                context,
                this.startPool,
                this.size,
                context.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(this),
                blockpos,
                false,
                this.projectStartToHeightmap,
                topClipOff,
                bottomClipOff,
                null,
                this.maxDistanceFromCenter,
                rotationString,
                this.buryingType,
                this.liquidSettings,
                (structurePiecesBuilder, pieces) -> postLayoutAdjustments(structurePiecesBuilder, context, offsetY, blockpos, finalTopClipOff, finalBottomClipOff, pieces));
    }

    @Override
    public StructureType<?> type() {
        return IAStructures.BIOME_FACING_STRUCTURE.get();
    }
}