package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashSet;

public class ReplaceLiquidOnlyProcessor implements StructureProcessor {
    public static final MapCodec<ReplaceLiquidOnlyProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.listOf()
                    .xmap(Sets::newHashSet, Lists::newArrayList)
                    .optionalFieldOf("blocks_to_always_place", new HashSet<>())
                    .forGetter(config -> config.blocksToAlwaysPlace)
    ).apply(instance, ReplaceLiquidOnlyProcessor::new));

    public final HashSet<BlockState> blocksToAlwaysPlace;

    private ReplaceLiquidOnlyProcessor(HashSet<BlockState> blocksToAlwaysPlace) {
        this.blocksToAlwaysPlace = blocksToAlwaysPlace;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos piecePos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if (!blocksToAlwaysPlace.contains(structureBlockInfoWorld.state())) {
            BlockPos position = structureBlockInfoWorld.pos();
            BlockState worldState = worldView.getBlockState(position);
            BlockState aboveWorldState = worldView.getBlockState(position.above());

            if (!worldState.getFluidState().isEmpty() && !structureBlockInfoWorld.state().hasBlockEntity() && !aboveWorldState.hasBlockEntity()) {
                structureBlockInfoWorld = new StructureTemplate.StructureBlockInfo(structureBlockInfoWorld.pos(), worldState, null);
            } else if (worldState.hasBlockEntity()) {
                structureBlockInfoWorld = new StructureTemplate.StructureBlockInfo(structureBlockInfoWorld.pos(), worldState, null);
            }
        }

        return structureBlockInfoWorld;
    }

    @Override
    public MapCodec<ReplaceLiquidOnlyProcessor> codec() {
        return CODEC;
    }
}

