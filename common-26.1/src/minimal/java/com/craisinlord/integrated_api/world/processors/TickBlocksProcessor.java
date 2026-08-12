package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.HashSet;

public class TickBlocksProcessor extends StructureProcessor {
    public static final MapCodec<TickBlocksProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("blocks_to_tick").orElse(new ArrayList<>()).xmap(HashSet::new, ArrayList::new).forGetter(config -> config.blocksToTick)
    ).apply(instance, TickBlocksProcessor::new));

    private final HashSet<Block> blocksToTick;

    private TickBlocksProcessor(HashSet<Block> blocksToTick) {
        this.blocksToTick = blocksToTick;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if (blocksToTick.contains(structureBlockInfoWorld.state().getBlock())) {
            if (levelReader instanceof WorldGenRegion worldGenRegion && !worldGenRegion.getCenter().equals(ChunkPos.containing(structureBlockInfoWorld.pos()))) {
                return structureBlockInfoWorld;
            }

            ChunkAccess chunk = levelReader.getChunk(structureBlockInfoWorld.pos());
            int minY = chunk.getMinY();
            int maxY = minY + chunk.getHeight() - 1;
            int currentY = structureBlockInfoWorld.pos().getY();
            if (currentY >= minY && currentY <= maxY) {
                ((LevelAccessor) levelReader).scheduleTick(structureBlockInfoWorld.pos(), structureBlockInfoWorld.state().getBlock(), 0);
            }
        }
        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return IAProcessors.TICK_BLOCKS_PROCESSOR.get();
    }
}
