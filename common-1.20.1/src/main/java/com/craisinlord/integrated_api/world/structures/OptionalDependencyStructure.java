package com.craisinlord.integrated_api.world.structures;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.IAStructures;
import com.craisinlord.integrated_api.utils.PlatformHooks;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptation;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptationType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.CheckerboardColumnBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.ArrayList;
import java.util.Optional;

public class   OptionalDependencyStructure extends JigsawStructure {

    public static final Codec<OptionalDependencyStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OptionalDependencyStructure.settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
            Codec.INT.optionalFieldOf("min_y_allowed").forGetter(structure -> structure.minYAllowed),
            Codec.INT.optionalFieldOf("max_y_allowed").forGetter(structure -> structure.maxYAllowed),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
            Codec.BOOL.fieldOf("cannot_spawn_in_liquid").orElse(false).forGetter(structure -> structure.cannotSpawnInLiquid),
            Codec.intRange(1, 100).optionalFieldOf("terrain_height_radius_check").forGetter(structure -> structure.terrainHeightCheckRadius),
            Codec.intRange(1, 1000).optionalFieldOf("allowed_terrain_height_range").forGetter(structure -> structure.allowedTerrainHeightRange),
            Codec.intRange(1, 100).optionalFieldOf("valid_biome_radius_check").forGetter(structure -> structure.biomeRadius),
            Codec.intRange(1, IntegratedAPI.NEW_STRUCTURE_SIZE).optionalFieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
            Codec.STRING.optionalFieldOf("required_mods").forGetter(structure -> structure.requiredMod),
            Codec.STRING.optionalFieldOf("illegal_mods").forGetter(structure -> structure.illegalMod),
            Codec.BOOL.fieldOf("rotation_fixed").orElse(false).forGetter(structure -> structure.rotationFixed),
            EnhancedTerrainAdaptationType.ADAPTATION_CODEC.optionalFieldOf("enhanced_terrain_adaptation", EnhancedTerrainAdaptation.NONE).forGetter(structure -> structure.enhancedTerrainAdaptation)
    ).apply(instance, OptionalDependencyStructure::new));

    public final Optional<String> requiredMod;
    public final Optional<String> illegalMod;

    public OptionalDependencyStructure(StructureSettings config,
                                 Holder<StructureTemplatePool> startPool,
                                 int size,
                                 Optional<Integer> minYAllowed,
                                 Optional<Integer> maxYAllowed,
                                 HeightProvider startHeight,
                                 Optional<Heightmap.Types> projectStartToHeightmap,
                                 boolean cannotSpawnInLiquid,
                                 Optional<Integer> terrainHeightCheckRadius,
                                 Optional<Integer> allowedTerrainHeightRange,
                                 Optional<Integer> biomeRadius,
                                 Optional<Integer> maxDistanceFromCenter,
                                 Optional<String> requiredMod,
                                 Optional<String> illegalMod,
                                 boolean rotationFixed,
                                 EnhancedTerrainAdaptation enhancedTerrainAdaptation) {
        super(config,
                startPool,
                size,
                minYAllowed,
                maxYAllowed,
                Optional.empty(),
                startHeight,
                projectStartToHeightmap,
                cannotSpawnInLiquid,
                terrainHeightCheckRadius,
                allowedTerrainHeightRange,
                biomeRadius,
                maxDistanceFromCenter,
                Optional.empty(),
                rotationFixed,
                enhancedTerrainAdaptation);
        this.requiredMod = requiredMod;
        this.illegalMod = illegalMod;
    }

    private ArrayList<String> convertModList(String modlist) {
        int startChar = 0;
        ArrayList<String> convertedModList = new ArrayList<>();
        for (int i = 0; i < modlist.length(); i++){
            if (modlist.charAt(i) == ','){
                convertedModList.add(modlist.substring(startChar, i));
                startChar = i + 1;
            }
        }
        convertedModList.add(modlist.substring(startChar));
        return convertedModList;
    }

    @Override
    protected boolean extraSpawningChecks(GenerationContext context, BlockPos blockPos) {
        ChunkPos chunkPos = context.chunkPos();

        if (!this.requiredMod.isEmpty()) {
            ArrayList<String> requiredMods = convertModList(this.requiredMod.get());
            for (String mod : requiredMods) {
                if (!isLoaded(mod)) {
                    IntegratedAPI.LOGGER.debug("Attempted to spawn Integrated API structure but not all required mods " + requiredMods + " are present.");
                    return false;
                }
            }
        }

        if (!this.illegalMod.isEmpty()) {
            ArrayList<String> illegalMods = convertModList(this.illegalMod.get());
            for (String mod : illegalMods) {
                if (isLoaded(mod)) {
                    IntegratedAPI.LOGGER.debug("Attempted to spawn Integrated API structure but illegal mods " + illegalMods + " are present.");
                    return false;
                }
            }
        }

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

        if (this.cannotSpawnInLiquid) {
            BlockPos centerOfChunk = chunkPos.getMiddleBlockPosition(0);
            int landHeight = context.chunkGenerator().getFirstOccupiedHeight(centerOfChunk.getX(), centerOfChunk.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            NoiseColumn columnOfBlocks = context.chunkGenerator().getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ(), context.heightAccessor(), context.randomState());
            BlockState topBlock = columnOfBlocks.getBlock(centerOfChunk.getY() + landHeight);

            if(!topBlock.getFluidState().isEmpty()) {
                return false;
            }
        }

        if (this.terrainHeightCheckRadius.isPresent() &&
                (this.allowedTerrainHeightRange.isPresent() || this.minYAllowed.isPresent()))
        {
            int maxTerrainHeight = Integer.MIN_VALUE;
            int minTerrainHeight = Integer.MAX_VALUE;
            int terrainCheckRange = this.terrainHeightCheckRadius.get();

            for (int curChunkX = chunkPos.x - terrainCheckRange; curChunkX <= chunkPos.x + terrainCheckRange; curChunkX++) {
                for (int curChunkZ = chunkPos.z - terrainCheckRange; curChunkZ <= chunkPos.z + terrainCheckRange; curChunkZ++) {
                    int height = context.chunkGenerator().getBaseHeight((curChunkX << 4) + 7, (curChunkZ << 4) + 7, this.projectStartToHeightmap.orElse(Heightmap.Types.WORLD_SURFACE_WG), context.heightAccessor(), context.randomState());
                    maxTerrainHeight = Math.max(maxTerrainHeight, height);
                    minTerrainHeight = Math.min(minTerrainHeight, height);

                    if (this.minYAllowed.isPresent() && minTerrainHeight < this.minYAllowed.get()) {
                        return false;
                    }

                    if (this.maxYAllowed.isPresent() && minTerrainHeight > this.maxYAllowed.get()) {
                        return false;
                    }
                }
            }

            if(this.allowedTerrainHeightRange.isPresent() &&
                    maxTerrainHeight - minTerrainHeight > this.allowedTerrainHeightRange.get())
            {
                return false;
            }
        }

        return true;
    }

    public boolean isLoaded(String name) {
        return PlatformHooks.isModLoaded(name);
    }

    @Override
    public StructureType<?> type() {
        return IAStructures.OPTIONAL_DEPENDENCY_STRUCTURE.get();
    }
}