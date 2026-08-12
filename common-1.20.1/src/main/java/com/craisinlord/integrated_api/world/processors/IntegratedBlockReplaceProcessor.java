package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.craisinlord.integrated_api.utils.PlatformHooks;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

/**
 * RANDOMLY REPLACES BLOCKS ON THE CONDITION THAT ANOTHER MOD IS INSTALLED
 */
public class IntegratedBlockReplaceProcessor extends StructureProcessor {

    public static final Codec<IntegratedBlockReplaceProcessor> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("input_block").forGetter(config -> config.inputBlock),
            CompoundTag.CODEC.optionalFieldOf("input_nbt").forGetter(config -> config.inputNbt),
            Codec.STRING.fieldOf("required_mod").forGetter(config -> config.requiredMod),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("output_block").forGetter(config -> config.outputBlock),
            BuiltInRegistries.BLOCK.byNameCodec().listOf().optionalFieldOf("output_blocks", ImmutableList.of()).forGetter(config -> config.outputBlocks),
            CompoundTag.CODEC.optionalFieldOf("output_nbt").forGetter(config -> config.outputNbt),
            Codec.floatRange(0, 1).fieldOf("probability").forGetter(config -> config.probability),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("otherwise_block").forGetter(config -> config.otherwiseBlock)
    ).apply(instance, instance.stable(IntegratedBlockReplaceProcessor::new)));

    private final Block inputBlock;
    private final Optional<CompoundTag> inputNbt;
    private final String requiredMod;
    private final Optional<Block> outputBlock;
    private final List<Block> outputBlocks;
    private final Optional<CompoundTag> outputNbt;
    private final float probability;
    private final Optional<Block> otherwiseBlock;

    private IntegratedBlockReplaceProcessor(Block inputBlock, Optional<CompoundTag> inputNbt, String requiredMod, Optional<Block> outputBlock, List<Block> outputBlocks, Optional<CompoundTag> outputNbt, float probability, Optional<Block> otherwiseBlock) {
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
                    BlockState actualNbt = structureBlockInfoWorld.state();
                    if (!(actualNbt == null) || !actualNbt.equals(requiredNbt)) {
                        return structureBlockInfoWorld;
                    }
                }
                RandomSource random = RandomSource.create();
                if(random.nextFloat() < probability) {
                    if (outputBlock.isPresent()) {
                        return createBlockInfo(structureBlockInfoWorld.pos(), outputBlock.get().defaultBlockState(), outputNbt, structureBlockInfoWorld.nbt());
                    }
                    else if(!outputBlocks.isEmpty()) {
                        Block selectedBlock = outputBlocks.get(random.nextInt(outputBlocks.size()));
                        return createBlockInfo(structureBlockInfoWorld.pos(), selectedBlock.defaultBlockState(), outputNbt, structureBlockInfoWorld.nbt());
                    }
                    else{
                        IntegratedAPI.LOGGER.warn("Integrated API: integrated_api:integrated_block_replace_processor in a processor file has no replacement block of any kind.");
                    }
                }
            } else if (otherwiseBlock.isPresent()) {
                return createBlockInfo(structureBlockInfoWorld.pos(), otherwiseBlock.get().defaultBlockState(), outputNbt, structureBlockInfoWorld.nbt());
            }
        }
        return structureBlockInfoWorld;
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