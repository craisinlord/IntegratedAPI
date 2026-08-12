package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * FIXES CRASH WITH FLUID TANKS ON GENERATION
 */
public class FluidTankProcessor extends StructureProcessor {

    public static final Codec<FluidTankProcessor> CODEC = Codec.unit(FluidTankProcessor::new);

    private FluidTankProcessor() { }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if(structureBlockInfoWorld.state().getBlock().getDescriptionId().equals("block.create.fluid_tank")) {
            CompoundTag originalTag = structureBlockInfoLocal.nbt();
            CompoundTag newTag = originalTag != null ? originalTag.copy() : new CompoundTag();
            newTag.remove("Luminosity");
            ((LevelAccessor) worldView).scheduleTick(structureBlockInfoWorld.pos(), structureBlockInfoWorld.state().getBlock(), 0);
            return new StructureTemplate.StructureBlockInfo(
                    structureBlockInfoWorld.pos(),
                    structureBlockInfoWorld.state(),
                    newTag.isEmpty() ? null : newTag);
        }
        return structureBlockInfoWorld;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return IAProcessors.FLUID_TANK_PROCESSOR.get();
    }
}
