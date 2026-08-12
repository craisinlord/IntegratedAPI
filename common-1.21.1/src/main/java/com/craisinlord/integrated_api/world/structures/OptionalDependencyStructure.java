package com.craisinlord.integrated_api.world.structures;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.IAStructures;
import com.craisinlord.integrated_api.utils.PlatformHooks;
import com.craisinlord.integrated_api.world.structures.codecs.YRangeAllowance;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptation;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptationType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class   OptionalDependencyStructure extends JigsawStructure {

    public static final MapCodec<OptionalDependencyStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            OptionalDependencyStructure.settingsCodec(instance),
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
            Codec.STRING.optionalFieldOf("required_mods").forGetter(structure -> structure.requiredMod),
            Codec.STRING.optionalFieldOf("illegal_mods").forGetter(structure -> structure.illegalMod),
            Codec.BOOL.fieldOf("rotation_fixed").orElse(false).forGetter(structure -> structure.rotationFixed),
            EnhancedTerrainAdaptationType.ADAPTATION_CODEC.optionalFieldOf("enhanced_terrain_adaptation", EnhancedTerrainAdaptation.NONE).forGetter(structure -> structure.enhancedTerrainAdaptation),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", net.minecraft.world.level.levelgen.structure.structures.JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings)
    ).apply(instance, OptionalDependencyStructure::new));

    public final Optional<String> requiredMod;
    public final Optional<String> illegalMod;

    public OptionalDependencyStructure(StructureSettings config,
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
                                 Optional<String> requiredMod,
                                 Optional<String> illegalMod,
                                 boolean rotationFixed,
                                 EnhancedTerrainAdaptation enhancedTerrainAdaptation,
                                 LiquidSettings liquidSettings) {
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
                rotationFixed,
                enhancedTerrainAdaptation,
                liquidSettings);
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
        boolean superCheck = super.extraSpawningChecks(context, blockPos);
        if(!superCheck)
            return false;
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