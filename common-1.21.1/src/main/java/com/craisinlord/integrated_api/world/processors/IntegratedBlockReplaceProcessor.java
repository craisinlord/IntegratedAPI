package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.craisinlord.integrated_api.utils.PlatformHooks;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RANDOMLY REPLACES BLOCKS ON THE CONDITION THAT ANOTHER MOD IS INSTALLED
 */
public class IntegratedBlockReplaceProcessor extends StructureProcessor {

    public static final MapCodec<IntegratedBlockReplaceProcessor> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("input_block").forGetter(config -> config.inputBlock),
            CompoundTag.CODEC.optionalFieldOf("input_nbt").forGetter(config -> config.inputNbt),
            Codec.STRING.fieldOf("required_mod").forGetter(config -> config.requiredMod),
            ResourceLocation.CODEC.optionalFieldOf("output_block").forGetter(config -> config.outputBlock),
            ResourceLocation.CODEC.listOf().optionalFieldOf("output_blocks", ImmutableList.of()).forGetter(config -> config.outputBlocks),
            CompoundTag.CODEC.optionalFieldOf("output_nbt").forGetter(config -> config.outputNbt),
            Codec.floatRange(0, 1).fieldOf("probability").forGetter(config -> config.probability),
            ResourceLocation.CODEC.optionalFieldOf("otherwise_block").forGetter(config -> config.otherwiseBlock)
    ).apply(instance, instance.stable(IntegratedBlockReplaceProcessor::new)));

    private static final Set<ResourceLocation> LOGGED_MISSING_BLOCKS = ConcurrentHashMap.newKeySet();

    private final Block inputBlock;
    private final Optional<CompoundTag> inputNbt;
    private final String requiredMod;
    private final Optional<ResourceLocation> outputBlock;
    private final List<ResourceLocation> outputBlocks;
    private final Optional<CompoundTag> outputNbt;
    private final float probability;
    private final Optional<ResourceLocation> otherwiseBlock;

    private IntegratedBlockReplaceProcessor(Block inputBlock, Optional<CompoundTag> inputNbt, String requiredMod, Optional<ResourceLocation> outputBlock, List<ResourceLocation> outputBlocks, Optional<CompoundTag> outputNbt, float probability, Optional<ResourceLocation> otherwiseBlock) {
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
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if (structureBlockInfoWorld.state().getBlock() == inputBlock){
            if(PlatformHooks.isModLoaded(requiredMod)) {
                // Check input NBT if specified
                if (inputNbt.isPresent()) {
                    CompoundTag requiredNbt = inputNbt.get();
                    CompoundTag actualNbt = structureBlockInfoWorld.nbt();
                    if (actualNbt == null || !actualNbt.equals(requiredNbt)) {
                        return structureBlockInfoWorld;
                    }
                }
                RandomSource random = structurePlacementData.getRandom(structureBlockInfoWorld.pos());
                if(random.nextFloat() < probability) {
                    if (outputBlock.isPresent()) {
                        Optional<Block> resolvedOutput = resolveBlock(outputBlock.get());
                        if (resolvedOutput.isPresent()) {
                            return createBlockInfo(structureBlockInfoWorld.pos(), resolvedOutput.get().defaultBlockState(), outputNbt, structureBlockInfoWorld.nbt());
                        }
                    }

                    List<Block> validOutputBlocks = outputBlocks.stream().map(IntegratedBlockReplaceProcessor::resolveBlock).flatMap(Optional::stream).toList();
                    if(!validOutputBlocks.isEmpty()) {
                        Block selectedBlock = validOutputBlocks.get(random.nextInt(validOutputBlocks.size()));
                        return createBlockInfo(structureBlockInfoWorld.pos(), selectedBlock.defaultBlockState(), outputNbt, structureBlockInfoWorld.nbt());
                    }
                    else{
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

    private static Optional<Block> resolveBlock(ResourceLocation blockId) {
        if (!BuiltInRegistries.BLOCK.containsKey(blockId)) {
            if (LOGGED_MISSING_BLOCKS.add(blockId)) {
                IntegratedAPI.LOGGER.warn("Integrated API: Block '{}' used by integrated_block_replace_processor was not found in the block registry. Skipping it.", blockId);
            }
            return Optional.empty();
        }
        return Optional.of(BuiltInRegistries.BLOCK.get(blockId));
    }

    private StructureTemplate.StructureBlockInfo createBlockInfo(BlockPos pos, BlockState state,
                                                                 Optional<CompoundTag> outputNbt, CompoundTag originalNbt) {
        CompoundTag newNbt = outputNbt.map(CompoundTag::copy).orElse(originalNbt);
        return new StructureTemplate.StructureBlockInfo(pos, state, newNbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return IAProcessors.INTEGRATED_BLOCK_REPLACE_PROCESSOR.get();
    }
}
