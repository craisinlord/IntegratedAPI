package com.craisinlord.integrated_api.world.processors.cobblemon;

import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.craisinlord.integrated_api.utils.GeneralUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CobblemonTrainerProcessor extends StructureProcessor {
    private static final Set<SpawnLocationKey> PROCESSED_SPAWN_LOCATIONS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static final MapCodec<CobblemonTrainerProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("input_blocks").orElse(new ArrayList<>()).xmap(HashSet::new, ArrayList::new).forGetter(processor -> processor.inputBlocks),
            WeightedTrainer.CODEC.listOf().fieldOf("trainers").orElse(List.of()).forGetter(processor -> processor.trainers)
    ).apply(instance, instance.stable(CobblemonTrainerProcessor::new)));

    private final HashSet<Block> inputBlocks;
    private final List<WeightedTrainer> trainers;

    public CobblemonTrainerProcessor(HashSet<Block> inputBlocks, List<WeightedTrainer> trainers) {
        this.inputBlocks = new HashSet<>(inputBlocks);
        this.trainers = List.copyOf(trainers);
    }

    @Override
    public @NotNull List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor serverLevelAccessor, BlockPos nbtOriginPos, BlockPos chunkCenter, List<StructureTemplate.StructureBlockInfo> nbtOriginBlockInfo, List<StructureTemplate.StructureBlockInfo> worldOriginBlockInfo, StructurePlaceSettings structurePlaceSettings) {
        if (inputBlocks.isEmpty() || trainers.isEmpty()) {
            return worldOriginBlockInfo;
        }

        Set<TrainerSpawnRequest> spawnRequests = new LinkedHashSet<>();
        for (int i = worldOriginBlockInfo.size() - 1; i >= 0; i--) {
            StructureTemplate.StructureBlockInfo blockInfo = worldOriginBlockInfo.get(i);
            if (!inputBlocks.contains(blockInfo.state().getBlock())) {
                continue;
            }

            Identifier trainerId = pickTrainer(structurePlaceSettings.getRandom(blockInfo.pos()));
            if (trainerId != null) {
                spawnRequests.add(new TrainerSpawnRequest(blockInfo.pos(), trainerId));
            }

            worldOriginBlockInfo.remove(i);
        }

        if (!spawnRequests.isEmpty()) {
            ServerLevel serverLevel = serverLevelAccessor.getLevel();
            MinecraftServer server = serverLevel.getServer();
            server.execute(() -> spawnRequests.forEach(request -> summonTrainer(server, serverLevel, request)));
        }

        return worldOriginBlockInfo;
    }

    private Identifier pickTrainer(RandomSource random) {
        List<Pair<Identifier, Integer>> weightedEntries = new ArrayList<>(trainers.size());
        for (WeightedTrainer trainer : trainers) {
            weightedEntries.add(Pair.of(trainer.trainer(), trainer.weight()));
        }
        return weightedEntries.isEmpty() ? null : GeneralUtils.getRandomEntry(weightedEntries, random);
    }

    private static void summonTrainer(MinecraftServer server, ServerLevel serverLevel, TrainerSpawnRequest request) {
        SpawnLocationKey spawnLocationKey = new SpawnLocationKey(serverLevel.dimension().location(), request.pos());
        if (!PROCESSED_SPAWN_LOCATIONS.add(spawnLocationKey)) {
            return;
        }

        BlockPos commandPos = request.pos().above();
        Vec3 spawnPos = Vec3.atBottomCenterOf(request.pos());
        CommandSourceStack source = server.createCommandSourceStack()
                .withPermission(2)
                .withLevel(serverLevel)
                .withPosition(spawnPos);

        String command = String.format(Locale.ROOT,
                "rctmod trainer summon_persistent %s %d %d %d",
                getTrainerCommandId(request.trainerId()),
                commandPos.getX(),
                commandPos.getY(),
                commandPos.getZ());

        server.getCommands().performPrefixedCommand(source.withSuppressedOutput(), command);
    }

    private static String getTrainerCommandId(Identifier trainerId) {
        return "rctmod".equals(trainerId.getNamespace()) ? trainerId.getPath() : trainerId.toString();
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return IAProcessors.COBBLEMON_TRAINER_PROCESSOR.get();
    }

    public record WeightedTrainer(Identifier trainer, int weight) {
        public static final Codec<WeightedTrainer> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Identifier.CODEC.fieldOf("trainer").forGetter(WeightedTrainer::trainer),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight").forGetter(WeightedTrainer::weight)
        ).apply(instance, WeightedTrainer::new));
    }

    private record TrainerSpawnRequest(BlockPos pos, Identifier trainerId) {}
    private record SpawnLocationKey(Identifier dimension, BlockPos pos) {}
}
