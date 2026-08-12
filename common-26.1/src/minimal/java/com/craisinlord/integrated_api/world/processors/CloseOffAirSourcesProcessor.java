package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.craisinlord.integrated_api.utils.GeneralUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluid;

public class CloseOffAirSourcesProcessor extends StructureProcessor {
    public static final MapCodec<CloseOffAirSourcesProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.mapPair(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block"), Codec.intRange(1, Integer.MAX_VALUE).fieldOf("weight"))
                    .codec().listOf().fieldOf("weighted_list_of_replacement_blocks")
                    .forGetter(processor -> processor.weightedReplacementBlocks)
    ).apply(instance, instance.stable(CloseOffAirSourcesProcessor::new)));

    private final List<Pair<Block, Integer>> weightedReplacementBlocks;

    public CloseOffAirSourcesProcessor(List<Pair<Block, Integer>> weightedReplacementBlocks) {
        this.weightedReplacementBlocks = weightedReplacementBlocks;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        ChunkPos currentChunkPos = ChunkPos.containing(infoIn2.pos());
        if (levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(currentChunkPos)) {
            return infoIn2;
        }

        if (!infoIn2.state().getFluidState().isEmpty()) {
            ChunkAccess currentChunk = levelReader.getChunk(currentChunkPos.x(), currentChunkPos.z());
            Fluid currentFluid = infoIn2.state().getFluidState().getType();
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (Direction direction : Direction.values()) {
                mutable.set(infoIn2.pos()).move(direction);
                if (mutable.getY() < currentChunk.getMinY() || mutable.getY() >= currentChunk.getMinY() + currentChunk.getHeight()) {
                    continue;
                }

                if (currentChunkPos.x() != mutable.getX() >> 4 || currentChunkPos.z() != mutable.getZ() >> 4) {
                    currentChunk = levelReader.getChunk(mutable);
                    currentChunkPos = ChunkPos.containing(mutable);
                }

                LevelHeightAccessor levelHeightAccessor = currentChunk.getHeightAccessorForGeneration();
                if (levelReader instanceof WorldGenLevel && mutable.getY() >= levelHeightAccessor.getMinY() && mutable.getY() < levelHeightAccessor.getMinY() + levelHeightAccessor.getHeight()) {
                    int sectionYIndex = currentChunk.getSectionIndex(mutable.getY());
                    LevelChunkSection levelChunkSection = currentChunk.getSection(sectionYIndex);
                    if (levelChunkSection == null) {
                        continue;
                    }

                    BlockState neighboringState = levelChunkSection.getBlockState(
                            SectionPos.sectionRelative(mutable.getX()),
                            SectionPos.sectionRelative(mutable.getY()),
                            SectionPos.sectionRelative(mutable.getZ()));

                    if (neighboringState.isAir() || (neighboringState.getBlock() instanceof LiquidBlock && !currentFluid.equals(neighboringState.getFluidState().getType()))) {
                        Block replacementBlock;
                        if (weightedReplacementBlocks.size() == 1) {
                            replacementBlock = weightedReplacementBlocks.get(0).getFirst();
                        } else {
                            RandomSource random = settings.getRandom(infoIn2.pos());
                            replacementBlock = GeneralUtils.getRandomEntry(weightedReplacementBlocks, random);
                        }

                        levelChunkSection.setBlockState(
                                SectionPos.sectionRelative(mutable.getX()),
                                SectionPos.sectionRelative(mutable.getY()),
                                SectionPos.sectionRelative(mutable.getZ()),
                                replacementBlock.defaultBlockState());
                    }
                }
            }
        }

        return infoIn2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return IAProcessors.CLOSE_OFF_AIR_SOURCES_PROCESSOR.get();
    }
}
