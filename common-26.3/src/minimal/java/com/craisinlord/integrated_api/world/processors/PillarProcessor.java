package com.craisinlord.integrated_api.world.processors;

import com.craisinlord.integrated_api.modinit.IATags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

/**
 * Extends blocks at the bottom of a piece straight down until they reach solid ground, the same idea as the
 * supports under a woodland mansion. Works through air, water, plants and anything in the pillar_replaceable block tag.
 * <p>
 * A block starts a pillar when all of these are true:
 * - it matches input_predicate (checked after the processors before this one have run)
 * - its height inside the piece is between min_local_y and max_local_y
 * - it is the lowest block of the piece in its column
 */
public class PillarProcessor implements StructureProcessor {

    public record PillarBlock(BlockState state, int weight) {
        public static final Codec<PillarBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("state").forGetter(PillarBlock::state),
                Codec.intRange(1, 5000).fieldOf("weight").orElse(1).forGetter(PillarBlock::weight)
        ).apply(instance, PillarBlock::new));
    }

    public static final MapCodec<PillarProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RuleTest.CODEC.fieldOf("input_predicate").forGetter(config -> config.inputPredicate),
            PillarBlock.CODEC.listOf().fieldOf("pillar_blocks").orElse(List.of()).forGetter(config -> config.pillarBlocks),
            Codec.INT.fieldOf("min_local_y").orElse(0).forGetter(config -> config.minLocalY),
            Codec.INT.fieldOf("max_local_y").orElse(0).forGetter(config -> config.maxLocalY),
            Codec.intRange(1, 4096).fieldOf("max_length").orElse(4096).forGetter(config -> config.maxLength),
            Codec.intRange(0, 64).fieldOf("embed_depth").orElse(0).forGetter(config -> config.embedDepth),
            Codec.BOOL.fieldOf("require_ground").orElse(true).forGetter(config -> config.requireGround)
    ).apply(instance, PillarProcessor::new));

    private final RuleTest inputPredicate;
    private final List<PillarBlock> pillarBlocks;
    private final int minLocalY;
    private final int maxLocalY;
    private final int maxLength;
    private final int embedDepth;
    private final boolean requireGround;

    private PillarProcessor(RuleTest inputPredicate, List<PillarBlock> pillarBlocks, int minLocalY, int maxLocalY, int maxLength, int embedDepth, boolean requireGround) {
        this.inputPredicate = inputPredicate;
        this.pillarBlocks = pillarBlocks;
        this.minLocalY = minLocalY;
        this.maxLocalY = maxLocalY;
        this.maxLength = maxLength;
        this.embedDepth = embedDepth;
        this.requireGround = requireGround;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level, BlockPos piecePos, BlockPos pivotPos, List<StructureTemplate.StructureBlockInfo> originalBlocks, List<StructureTemplate.StructureBlockInfo> processedBlocks, StructurePlaceSettings settings) {
        BoundingBox box = settings.getBoundingBox();

        // Pieces are placed once per chunk they touch. Only handle columns inside the chunk being placed right now
        // so every pillar is built exactly once and never reads or writes outside the loaded region.
        Long2ObjectMap<StructureTemplate.StructureBlockInfo> lowestBlocks = new Long2ObjectOpenHashMap<>();
        for (StructureTemplate.StructureBlockInfo info : processedBlocks) {
            BlockPos pos = info.pos();
            if (box != null && (pos.getX() < box.minX() || pos.getX() > box.maxX() || pos.getZ() < box.minZ() || pos.getZ() > box.maxZ())) {
                continue;
            }
            if (info.state().isAir() || info.state().is(Blocks.STRUCTURE_VOID) || info.state().is(Blocks.WATER) || info.state().is(Blocks.LAVA)) {
                continue;
            }

            long column = ChunkPos.pack(pos.getX(), pos.getZ());
            StructureTemplate.StructureBlockInfo lowest = lowestBlocks.get(column);
            if (lowest == null || pos.getY() < lowest.pos().getY()) {
                lowestBlocks.put(column, info);
            }
        }

        for (StructureTemplate.StructureBlockInfo foot : lowestBlocks.values()) {
            // Pieces only ever rotate around the Y axis, so this is the height inside the nbt.
            int localY = foot.pos().getY() - piecePos.getY();
            if (localY < this.minLocalY || localY > this.maxLocalY) {
                continue;
            }

            RandomSource random = settings.getRandom(foot.pos());
            if (this.inputPredicate.test(foot.state(), foot.pos(), random)) {
                buildPillar(level, foot, random);
            }
        }

        return processedBlocks;
    }

    private void buildPillar(ServerLevelAccessor level, StructureTemplate.StructureBlockInfo foot, RandomSource random) {
        int minY = level.getMinY();
        BlockPos.MutableBlockPos mutable = foot.pos().mutable();
        int topY = foot.pos().getY() - 1;
        int lowestAllowedY = Math.max(minY, topY - this.maxLength + 1);

        // Find the ground first so nothing is built over the void.
        int groundY = topY;
        // Ground has to be at least two blocks thick, otherwise the pillar would rest on a thin crust above a water pocket or cave.
        while (groundY >= lowestAllowedY && (canReplace(level.getBlockState(mutable.setY(groundY))) || canReplace(level.getBlockState(mutable.setY(groundY - 1))))) {
            groundY--;
        }
        if (groundY < lowestAllowedY && this.requireGround) {
            return;
        }

        int bottomY = Math.max(minY, groundY + 1 - this.embedDepth);
        for (int y = topY; y >= bottomY; y--) {
            mutable.setY(y);
            BlockState existing = level.getBlockState(mutable);
            if (existing.is(Blocks.BEDROCK)) {
                return;
            }

            BlockState state = pickState(foot.state(), random);
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, existing.getFluidState().is(FluidTags.WATER));
            }
            level.setBlock(mutable, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    private BlockState pickState(BlockState footState, RandomSource random) {
        if (this.pillarBlocks.isEmpty()) {
            return footState;
        }

        int totalWeight = 0;
        for (PillarBlock pillarBlock : this.pillarBlocks) {
            totalWeight += pillarBlock.weight();
        }

        int roll = random.nextInt(totalWeight);
        for (PillarBlock pillarBlock : this.pillarBlocks) {
            roll -= pillarBlock.weight();
            if (roll < 0) {
                return pillarBlock.state();
            }
        }
        return footState;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty() || state.canBeReplaced() || state.is(IATags.PILLAR_REPLACEABLE);
    }

    @Override
    public MapCodec<PillarProcessor> codec() {
        return CODEC;
    }
}
