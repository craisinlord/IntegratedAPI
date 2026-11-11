package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class WaterloggingFixProcessor extends StructureProcessor {

    public static final MapCodec<WaterloggingFixProcessor> CODEC = MapCodec.unit(WaterloggingFixProcessor::new);

    private WaterloggingFixProcessor() { }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        if (infoIn2.state().hasProperty(BlockStateProperties.WATERLOGGED)
                && infoIn2.state().getValue(BlockStateProperties.WATERLOGGED)) {
            return new StructureTemplate.StructureBlockInfo(
                    infoIn2.pos(),
                    infoIn2.state().setValue(BlockStateProperties.WATERLOGGED, false),
                    infoIn2.nbt());
        }
        return infoIn2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return IAProcessors.WATERLOGGING_FIX_PROCESSOR.get();
    }
}
