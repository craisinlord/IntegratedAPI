package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.craisinlord.integrated_api.utils.PlatformHooks;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class IntegratedBlockReplaceProcessor implements StructureProcessor {
    public static final MapCodec<IntegratedBlockReplaceProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("input_block").forGetter(config -> config.inputBlock),
            CompoundTag.CODEC.optionalFieldOf("input_nbt").forGetter(config -> config.inputNbt),
            Codec.STRING.fieldOf("required_mod").forGetter(config -> config.requiredMod),
            Identifier.CODEC.optionalFieldOf("output_block").forGetter(config -> config.outputBlock),
            Identifier.CODEC.listOf().optionalFieldOf("output_blocks", ImmutableList.of()).forGetter(config -> config.outputBlocks),
            CompoundTag.CODEC.optionalFieldOf("output_nbt").forGetter(config -> config.outputNbt),
            Codec.floatRange(0, 1).fieldOf("probability").forGetter(config -> config.probability),
            Identifier.CODEC.optionalFieldOf("otherwise_block").forGetter(config -> config.otherwiseBlock)
    ).apply(instance, instance.stable(IntegratedBlockReplaceProcessor::new)));

    private static final Set<Identifier> LOGGED_MISSING_BLOCKS = ConcurrentHashMap.newKeySet();

    private final Block inputBlock;
    private final Optional<CompoundTag> inputNbt;
    private final String requiredMod;
    private final Optional<Identifier> outputBlock;
    private final List<Identifier> outputBlocks;
    private final Optional<CompoundTag> outputNbt;
    private final float probability;
    private final Optional<Identifier> otherwiseBlock;

    private IntegratedBlockReplaceProcessor(Block inputBlock, Optional<CompoundTag> inputNbt, String requiredMod, Optional<Identifier> outputBlock, List<Identifier> outputBlocks, Optional<CompoundTag> outputNbt, float probability, Optional<Identifier> otherwiseBlock) {
        this.inputBlock = inputBlock;
        this.inputNbt = inputNbt;
        this.requiredMod = requiredMod;
        this.outputBlock = outputBlock;
        this.outputBlocks = outputBlocks;
        this.outputNbt = outputNbt;
        this.probability = probability;
        this.otherwiseBlock = otherwiseBlock;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos piecePos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if (structureBlockInfoWorld.state().getBlock() == inputBlock) {
            if (PlatformHooks.isModLoaded(requiredMod)) {
                if (inputNbt.isPresent()) {
                    CompoundTag requiredNbt = inputNbt.get();
                    CompoundTag actualNbt = structureBlockInfoWorld.nbt();
                    if (actualNbt == null || !actualNbt.equals(requiredNbt)) {
                        return structureBlockInfoWorld;
                    }
                }
                RandomSource random = structurePlacementData.getRandom(structureBlockInfoWorld.pos());
                if (random.nextFloat() < probability) {
                    if (outputBlock.isPresent()) {
                        Optional<Block> resolvedOutput = resolveBlock(outputBlock.get());
                        if (resolvedOutput.isPresent()) {
                            return createBlockInfo(structureBlockInfoWorld.pos(), resolvedOutput.get().defaultBlockState(), outputNbt, structureBlockInfoWorld.nbt());
                        }
                    }

                    List<Block> validOutputBlocks = outputBlocks.stream().map(IntegratedBlockReplaceProcessor::resolveBlock).flatMap(Optional::stream).toList();
                    if (!validOutputBlocks.isEmpty()) {
                        Block selectedBlock = validOutputBlocks.get(random.nextInt(validOutputBlocks.size()));
                        return createBlockInfo(structureBlockInfoWorld.pos(), selectedBlock.defaultBlockState(), outputNbt, structureBlockInfoWorld.nbt());
                    } else {
                        IntegratedAPI.LOGGER.warn("Integrated API: integrated_api:integrated_block_replace_processor in a processor file has no replacement block of any kind.");
                    }
                }
            } else if (otherwiseBlock.isPresent()) {
                Optional<Block> resolvedOtherwiseBlock = resolveBlock(otherwiseBlock.get());
                if (resolvedOtherwiseBlock.isPresent()) {
                    return createBlockInfo(structureBlockInfoWorld.pos(), resolvedOtherwiseBlock.get().defaultBlockState(), outputNbt, structureBlockInfoWorld.nbt());
                }
            }
        }
        return structureBlockInfoWorld;
    }

    private static Optional<Block> resolveBlock(Identifier blockId) {
        if (!BuiltInRegistries.BLOCK.containsKey(blockId)) {
            if (LOGGED_MISSING_BLOCKS.add(blockId)) {
                IntegratedAPI.LOGGER.warn("Integrated API: Block '{}' used by integrated_block_replace_processor was not found in the block registry. Skipping it.", blockId);
            }
            return Optional.empty();
        }
        return BuiltInRegistries.BLOCK.get(blockId).map(holder -> holder.value());
    }

    private StructureTemplate.StructureBlockInfo createBlockInfo(BlockPos pos, BlockState state, Optional<CompoundTag> outputNbt, CompoundTag originalNbt) {
        CompoundTag newNbt = outputNbt.map(CompoundTag::copy).orElse(originalNbt);
        return new StructureTemplate.StructureBlockInfo(pos, state, newNbt);
    }

    @Override
    public MapCodec<IntegratedBlockReplaceProcessor> codec() {
        return CODEC;
    }
}

