package com.craisinlord.integrated_api.world.processors.create;

import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * ACTIVATES MECHANICAL BEARINGS ON GENERATION
 */
public class MechanicalBearingProcessor implements StructureProcessor {

    public static final MapCodec<MechanicalBearingProcessor> CODEC = MapCodec.unit(MechanicalBearingProcessor::new);

    private MechanicalBearingProcessor() { }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos piecePos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if(structureBlockInfoWorld.state().getBlock().getDescriptionId().equals("block.create.mechanical_bearing")) {
            CompoundTag compoundTag = structureBlockInfoWorld.nbt();
            compoundTag.putBoolean("QueueAssembly", true);
            ((LevelAccessor) worldView).scheduleTick(structureBlockInfoWorld.pos(), structureBlockInfoWorld.state().getBlock(), 0);
            return new StructureTemplate.StructureBlockInfo(
                    structureBlockInfoWorld.pos(),
                    structureBlockInfoWorld.state(),
                    compoundTag);
        }
        return structureBlockInfoWorld;
    }

    @Override
    public MapCodec<MechanicalBearingProcessor> codec() {
        return CODEC;
    }
}

