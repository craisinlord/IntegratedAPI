package com.craisinlord.integrated_api.world.structures;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.IAStructures;
import com.craisinlord.integrated_api.utils.PlatformHooks;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.ArrayList;
import java.util.Optional;

public class ModAdaptiveStructure extends JigsawStructure {

    public static final MapCodec<ModAdaptiveStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ModAdaptiveStructure.settingsCodec(instance),
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
            Codec.BOOL.fieldOf("rotation_fixed").orElse(false).forGetter(structure -> structure.rotationFixed),
            EnhancedTerrainAdaptationType.ADAPTATION_CODEC.optionalFieldOf("enhanced_terrain_adaptation", EnhancedTerrainAdaptation.NONE).forGetter(structure -> structure.enhancedTerrainAdaptation),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", net.minecraft.world.level.levelgen.structure.structures.JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings),
            Codec.STRING.fieldOf("change_pool_mods").forGetter(structure -> structure.changePoolMod),
            StructureTemplatePool.CODEC.fieldOf("new_pool").forGetter(structure -> structure.newPool)
    ).apply(instance, ModAdaptiveStructure::new));

    public final String changePoolMod;
    public final Holder<StructureTemplatePool> newPool;

    public ModAdaptiveStructure(StructureSettings config,
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
                                boolean rotationFixed,
                                EnhancedTerrainAdaptation enhancedTerrainAdaptation,
                                LiquidSettings liquidSettings,
                                String changePoolMod,
                                Holder<StructureTemplatePool> newPool) {
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
        this.changePoolMod = changePoolMod;
        this.newPool = newPool;
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

    public boolean allModsPresent(ArrayList<String> convertedModList) {
        for (String mod : convertedModList) {
            if (!isLoaded(mod)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int offsetY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos blockpos = new BlockPos(context.chunkPos().getMinBlockX(), offsetY, context.chunkPos().getMinBlockZ());
        if (!extraSpawningChecks(context, blockpos)) {
            return Optional.empty();
        }

        Holder<StructureTemplatePool> finalPool = startPool;

        if (!this.changePoolMod.isEmpty()) {
            ArrayList<String> changePoolMods = convertModList(this.changePoolMod);
            if (allModsPresent(changePoolMods)) {
                finalPool = newPool;
            }
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
        String rotationString;
        if (rotationFixed) {
            rotationString = "NONE";
        } else {
            rotationString = "RANDOM";
        }

        int finalTopClipOff = topClipOff;
        int finalBottomClipOff = bottomClipOff;
        return PieceLimitedJigsawManager.assembleJigsawStructure(
                context,
                finalPool,
                this.size,
                context.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(this),
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

    public static boolean isLoaded(String name) {
        return PlatformHooks.isModLoaded(name);
    }

    @Override
    public StructureType<?> type() {
        return IAStructures.MOD_ADAPTIVE_STRUCTURE.get();
    }
}
