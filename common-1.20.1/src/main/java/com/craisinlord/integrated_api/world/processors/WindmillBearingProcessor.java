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
 * ACTIVATES WINDMILL BEARINGS ON GENERATION
 */
public class WindmillBearingProcessor extends StructureProcessor {

    public static final Codec<WindmillBearingProcessor> CODEC = Codec.unit(WindmillBearingProcessor::new);

    private WindmillBearingProcessor() { }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldView, BlockPos pos, BlockPos blockPos, StructureTemplate.StructureBlockInfo structureBlockInfoLocal, StructureTemplate.StructureBlockInfo structureBlockInfoWorld, StructurePlaceSettings structurePlacementData) {
        if(structureBlockInfoWorld.state().getBlock().getDescriptionId().equals("block.create.windmill_bearing")) {
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
    protected StructureProcessorType<?> getType() {
        return IAProcessors.WINDMILL_BEARING_PROCESSOR.get();
    }
}
