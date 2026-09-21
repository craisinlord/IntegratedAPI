package com.craisinlord.integrated_api.world.processors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.craisinlord.integrated_api.misc.mobspawners.MobSpawnerManager;
import com.craisinlord.integrated_api.modinit.IAProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public final class TrialSpawnerProcessor extends StructureProcessor {
    public static final MapCodec<TrialSpawnerProcessor> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceLocation.CODEC.fieldOf("loot_table").forGetter(c -> c.lootTable), ResourceLocation.CODEC.optionalFieldOf("ominous_loot_table").forGetter(c -> c.ominousLootTable),
            ResourceLocation.CODEC.fieldOf("integrated_api_spawner_resourcelocation").forGetter(c -> c.spawnerResourceLocation), InclusiveRange.INT.optionalFieldOf("valid_block_light_level").forGetter(c -> c.validBlockLightLevel),
            InclusiveRange.INT.optionalFieldOf("valid_sky_light_level").forGetter(c -> c.validSkyLightLevel), Codec.BOOL.fieldOf("replace_vanilla_spawners").orElse(false).forGetter(c -> c.replaceVanillaSpawners),
            CompoundTag.CODEC.optionalFieldOf("normal_config").forGetter(c -> c.normalConfig), CompoundTag.CODEC.optionalFieldOf("ominous_config").forGetter(c -> c.ominousConfig),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("target_cooldown_length").forGetter(c -> c.targetCooldownLength), Codec.intRange(1, 128).optionalFieldOf("required_player_range").forGetter(c -> c.requiredPlayerRange)
    ).apply(i, i.stable(TrialSpawnerProcessor::new)));
    private final ResourceLocation lootTable, spawnerResourceLocation;
    private final Optional<ResourceLocation> ominousLootTable;
    private final Optional<InclusiveRange<Integer>> validBlockLightLevel, validSkyLightLevel;
    private final boolean replaceVanillaSpawners;
    private final Optional<CompoundTag> normalConfig, ominousConfig;
    private final Optional<Integer> targetCooldownLength, requiredPlayerRange;

    private TrialSpawnerProcessor(ResourceLocation lootTable, Optional<ResourceLocation> ominousLootTable, ResourceLocation spawnerResourceLocation, Optional<InclusiveRange<Integer>> validBlockLightLevel, Optional<InclusiveRange<Integer>> validSkyLightLevel, boolean replaceVanillaSpawners, Optional<CompoundTag> normalConfig, Optional<CompoundTag> ominousConfig, Optional<Integer> targetCooldownLength, Optional<Integer> requiredPlayerRange) {
        this.lootTable = lootTable; this.ominousLootTable = ominousLootTable; this.spawnerResourceLocation = spawnerResourceLocation; this.validBlockLightLevel = validBlockLightLevel; this.validSkyLightLevel = validSkyLightLevel; this.replaceVanillaSpawners = replaceVanillaSpawners; this.normalConfig = normalConfig; this.ominousConfig = ominousConfig; this.targetCooldownLength = targetCooldownLength; this.requiredPlayerRange = requiredPlayerRange;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos pos, BlockPos piecePos, StructureTemplate.StructureBlockInfo localInfo, StructureTemplate.StructureBlockInfo worldInfo, StructurePlaceSettings settings) {
        boolean trial = worldInfo.state().getBlock() instanceof TrialSpawnerBlock;
        if (!trial && !(replaceVanillaSpawners && worldInfo.state().getBlock() instanceof SpawnerBlock)) return worldInfo;
        CompoundTag tag = worldInfo.nbt() == null ? new CompoundTag() : worldInfo.nbt().copy();
        RandomSource random = settings.getRandom(worldInfo.pos());
        apply(tag, "normal_config", normalConfig, lootTable, random); apply(tag, "ominous_config", ominousConfig, ominousLootTable.orElse(lootTable), random);
        targetCooldownLength.ifPresent(v -> tag.putInt("target_cooldown_length", v)); requiredPlayerRange.ifPresent(v -> tag.putInt("required_player_range", v));
        return new StructureTemplate.StructureBlockInfo(worldInfo.pos(), trial ? worldInfo.state() : Blocks.TRIAL_SPAWNER.defaultBlockState(), tag);
    }

    private void apply(CompoundTag root, String name, Optional<CompoundTag> override, ResourceLocation table, RandomSource random) {
        CompoundTag config = root.contains(name) ? root.getCompound(name).copy() : new CompoundTag();
        override.ifPresent(value -> value.getAllKeys().forEach(key -> config.put(key, value.get(key).copy())));
        ListTag tables = new ListTag(); CompoundTag entry = new CompoundTag(); entry.put("data", StringTag.valueOf(table.toString())); entry.putInt("weight", 1); tables.add(entry); config.put("loot_tables_to_eject", tables);
        EntityType<?> entity = MobSpawnerManager.MOB_SPAWNER_MANAGER.getSpawnerMob(spawnerResourceLocation, random);
        if (entity != null) {
            CompoundTag entityData = new CompoundTag(); entityData.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entity).toString()); CompoundTag spawnData = new CompoundTag(); spawnData.put("entity", entityData);
            if (validBlockLightLevel.isPresent() || validSkyLightLevel.isPresent()) { CompoundTag rules = new CompoundTag(); validBlockLightLevel.ifPresent(r -> { CompoundTag v = new CompoundTag(); v.putInt("min_inclusive", r.minInclusive()); v.putInt("max_inclusive", r.maxInclusive()); rules.put("block_light_limit", v); }); validSkyLightLevel.ifPresent(r -> { CompoundTag v = new CompoundTag(); v.putInt("min_inclusive", r.minInclusive()); v.putInt("max_exclusive", r.maxInclusive()); rules.put("sky_light_limit", v); }); spawnData.put("custom_spawn_rules", rules); }
            ListTag potentials = new ListTag(); CompoundTag potential = new CompoundTag(); potential.put("data", spawnData); potential.putInt("weight", 1); potentials.add(potential); config.put("spawn_potentials", potentials);
        }
        root.put(name, config);
    }

    @Override
    protected StructureProcessorType<?> getType() { return IAProcessors.TRIAL_SPAWNER_PROCESSOR.get(); }
}
