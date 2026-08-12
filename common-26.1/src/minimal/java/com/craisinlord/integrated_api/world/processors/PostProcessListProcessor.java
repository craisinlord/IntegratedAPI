package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.modinit.IAProcessors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class PostProcessListProcessor extends StructureProcessor {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<PostProcessListProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StructureProcessorType.SINGLE_CODEC.listOf().fieldOf("delegates").forGetter(processor -> processor.delegates)
    ).apply(instance, PostProcessListProcessor::new));

    private final List<StructureProcessor> delegates;

    public PostProcessListProcessor(List<StructureProcessor> structureProcessors) {
        this.delegates = structureProcessors;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor serverLevelAccessor, BlockPos nbtOriginPos, BlockPos chunkCenter, List<StructureTemplate.StructureBlockInfo> nbtOriginBlockInfo, List<StructureTemplate.StructureBlockInfo> worldOriginBlockInfo, StructurePlaceSettings structurePlaceSettings) {
        if (!worldOriginBlockInfo.isEmpty()) {
            if (nbtOriginBlockInfo.size() != worldOriginBlockInfo.size()) {
                int listSize = nbtOriginBlockInfo.size();
                LOGGER.warn("Original block info list not in sync with processed list, skipping processing. Original size: {}, Processed size: {}", listSize, worldOriginBlockInfo.size());
            } else {
                for (int i = 0; i < worldOriginBlockInfo.size(); i++) {
                    StructureTemplate.StructureBlockInfo structureBlockInfoOriginalNbtOrigin = nbtOriginBlockInfo.get(i);
                    StructureTemplate.StructureBlockInfo structureBlockInfoWorld = worldOriginBlockInfo.get(i);
                    StructureTemplate.StructureBlockInfo structureBlockInfoResult = structureBlockInfoWorld;

                    for (StructureProcessor processor : this.delegates) {
                        structureBlockInfoResult = processor.processBlock(
                                serverLevelAccessor,
                                structureBlockInfoOriginalNbtOrigin.pos(),
                                structureBlockInfoResult.pos(),
                                structureBlockInfoOriginalNbtOrigin,
                                structureBlockInfoResult,
                                structurePlaceSettings);

                        if (structureBlockInfoResult == null) {
                            break;
                        }
                    }

                    if (structureBlockInfoResult != null && !structureBlockInfoWorld.equals(structureBlockInfoResult)) {
                        worldOriginBlockInfo.set(i, structureBlockInfoResult);
                    }
                }
            }
        }

        return worldOriginBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return IAProcessors.POST_PROCESS_LIST_PROCESSOR.get();
    }
}
