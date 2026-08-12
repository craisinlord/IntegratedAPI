package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.misc.workstations.WorkstationManager;
import com.craisinlord.integrated_api.misc.workstations.WorkstationObj;
import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.craisinlord.integrated_api.utils.PlatformHooks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WorkstationProcessor extends StructureProcessor {

    public static final MapCodec<WorkstationProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("input_block").forGetter(config -> config.inputBlock),
            Codec.STRING.fieldOf("workstation_type").forGetter(config -> config.workstationType),
            Codec.BOOL.fieldOf("enable_integration").orElse(Boolean.TRUE).forGetter(config -> config.enableIntegration)
    ).apply(instance, instance.stable(WorkstationProcessor::new)));

    private static final Set<String> LOGGED_MISSING_TYPES = ConcurrentHashMap.newKeySet();

    private final Block inputBlock;
    private final String workstationType;
    private final boolean enableIntegration;

    public WorkstationProcessor(Block inputBlock, String workstationType, boolean enableIntegration) {
        this.inputBlock = inputBlock;
        this.workstationType = workstationType;
        this.enableIntegration = enableIntegration;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if(structureBlockInfoWorld.state().getBlock() == inputBlock) {
            RandomSource random = structurePlacementData.getRandom(structureBlockInfoWorld.pos());
            Optional<Block> replacement = pickRandomOutputBlock(workstationType, enableIntegration, random);
            if (replacement.isPresent()) {
                BlockState newBlockState = replacement.get().defaultBlockState();
                return new StructureTemplate.StructureBlockInfo(
                        structureBlockInfoWorld.pos(),
                        newBlockState,
                        structureBlockInfoWorld.nbt());
            }
        }
        return structureBlockInfoWorld;
    }

    private static Optional<Block> pickRandomOutputBlock(String workstationType, boolean enableIntegration, RandomSource random) {
        Identifier workstationTypeId = toWorkstationTypeId(workstationType);
        List<WorkstationObj> entries = WorkstationManager.WORKSTATION_MANAGER.getWorkstations(workstationTypeId);
        if (entries.isEmpty()) {
            logMissingType(workstationTypeId);
            return Optional.empty();
        }

        List<WeightedBlock> candidates = new ArrayList<>();
        int totalWeight = 0;

        for (WorkstationObj entry : entries) {
            if (!isEnabled(entry.requiredMod, enableIntegration)) {
                continue;
            }

            if (entry.outputBlock == null || !BuiltInRegistries.BLOCK.containsKey(entry.outputBlock)) {
                continue;
            }

            Block block = BuiltInRegistries.BLOCK.get(entry.outputBlock);
            candidates.add(new WeightedBlock(block, entry.weight));
            totalWeight += entry.weight;
        }

        if (candidates.isEmpty() || totalWeight <= 0) {
            return Optional.empty();
        }

        int pick = random.nextInt(totalWeight);
        for (WeightedBlock candidate : candidates) {
            pick -= candidate.weight;
            if (pick < 0) {
                return Optional.of(candidate.block);
            }
        }

        return Optional.of(candidates.get(candidates.size() - 1).block);
    }

    private static Identifier toWorkstationTypeId(String workstationType) {
        if (workstationType.indexOf(':') >= 0) {
            Identifier parsed = Identifier.tryParse(workstationType);
            if (parsed != null) {
                return parsed;
            }
        }
        return Identifier.fromNamespaceAndPath(IntegratedAPI.MODID, workstationType);
    }

    private static boolean isEnabled(String requiredMod, boolean enableIntegration) {
        if ("minecraft".equals(requiredMod)) {
            return true;
        }
        return enableIntegration && PlatformHooks.isModLoaded(requiredMod);
    }

    private static void logMissingType(Identifier workstationTypeId) {
        String key = workstationTypeId.toString();
        if (!LOGGED_MISSING_TYPES.add(key)) {
            return;
        }
        IntegratedAPI.LOGGER.error("(Integrated API Workstation Processor) Missing workstation data for type '{}' at data/{}/workstations/{}.json",
                workstationTypeId.getPath(),
                workstationTypeId.getNamespace(),
                workstationTypeId.getPath());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return IAProcessors.WORKSTATION_PROCESSOR.get();
    }

    private record WeightedBlock(Block block, int weight) {}
}
