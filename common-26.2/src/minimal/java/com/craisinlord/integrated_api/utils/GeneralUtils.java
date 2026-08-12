package com.craisinlord.integrated_api.utils;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import com.craisinlord.integrated_api.mixins.structures.JigsawJunctionAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class GeneralUtils {
    private static final Map<BlockState, Boolean> IS_FULLCUBE_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<HeightKey, Integer> CACHED_HEIGHT = new ConcurrentHashMap<>(2048);

    private GeneralUtils() {
    }

    public static <T> T getRandomEntry(List<Pair<T, Integer>> rlList, RandomSource random) {
        double totalWeight = 0.0;
        for (Pair<T, Integer> pair : rlList) {
            totalWeight += pair.getSecond();
        }

        int index = 0;
        for (double randomWeightPicked = random.nextFloat() * totalWeight; index < rlList.size() - 1; ++index) {
            randomWeightPicked -= rlList.get(index).getSecond();
            if (randomWeightPicked <= 0.0) {
                break;
            }
        }

        return rlList.get(index).getFirst();
    }

    public static boolean isFullCube(BlockGetter world, BlockPos pos, BlockState state) {
        if (state == null) {
            return false;
        }
        return IS_FULLCUBE_MAP.computeIfAbsent(state, stateIn -> Block.isShapeFullBlock(stateIn.getOcclusionShape()));
    }

    public static BlockState copyBlockProperties(BlockState oldBlockState, BlockState newBlockState) {
        for (Property<?> property : oldBlockState.getProperties()) {
            if (newBlockState.hasProperty(property)) {
                newBlockState = getStateWithProperty(newBlockState, oldBlockState, property);
            }
        }
        return newBlockState;
    }

    public static <T extends Comparable<T>> BlockState getStateWithProperty(BlockState state, BlockState stateToCopy, Property<T> property) {
        return state.setValue(property, stateToCopy.getValue(property));
    }

    public static void centerAllPieces(BlockPos targetPos, List<? extends StructurePiece> pieces) {
        if (pieces.isEmpty()) {
            return;
        }
        BlockPos structureCenter = pieces.get(0).getBoundingBox().getCenter();
        int xOffset = targetPos.getX() - structureCenter.getX();
        int zOffset = targetPos.getZ() - structureCenter.getZ();
        for (StructurePiece structurePiece : pieces) {
            structurePiece.move(xOffset, 0, zOffset);
        }
    }

    public static void movePieceProperly(StructurePiece piece, int x, int y, int z) {
        piece.move(x, y, z);
        if (piece instanceof PoolElementStructurePiece poolPiece) {
            poolPiece.getJunctions().forEach(junction -> {
                JigsawJunctionAccessor accessor = (JigsawJunctionAccessor) junction;
                accessor.setSourceX(junction.getSourceX() + x);
                accessor.setSourceGroundY(junction.getSourceGroundY() + y);
                accessor.setSourceZ(junction.getSourceZ() + z);
            });
        }
    }

    public static boolean canJigsawsAttach(StructureTemplate.JigsawBlockInfo first, StructureTemplate.JigsawBlockInfo second) {
        return JigsawBlock.canAttach(first, second);
    }

    public static List<StructureStart> inboundsValidStartsForAllStructure(WorldGenLevel level, BlockPos position, Predicate<Structure> structureMatch) {
        StructureManager structureManager = level.getLevel().structureManager();
        SectionPos sectionPos = SectionPos.of(position);
        ChunkAccess chunkAccess = level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES);
        if (!chunkAccess.getPersistedStatus().isOrAfter(ChunkStatus.STRUCTURE_REFERENCES)) {
            return List.of();
        }

        Map<Structure, LongSet> references = chunkAccess.getAllReferences();
        List<StructureStart> starts = new ArrayList<>();
        for (Map.Entry<Structure, LongSet> entry : references.entrySet()) {
            if (structureMatch.test(entry.getKey())) {
                fillStartsForStructure(level, structureManager, entry.getKey(), entry.getValue(), position, starts::add);
            }
        }
        return starts;
    }

    public static void fillStartsForStructure(LevelReader level, StructureManager structureManager, Structure structure, LongSet references, BlockPos position, Consumer<StructureStart> consumer) {
        for (long ref : references) {
            SectionPos sectionPos = SectionPos.of(ChunkPos.unpack(ref), level.getMinSectionY());
            if (!level.hasChunk(sectionPos.x(), sectionPos.z())) {
                continue;
            }

            StructureStart structureStart = structureManager.getStartForStructure(sectionPos, structure, level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_STARTS));
            if (structureStart != null && structureStart.isValid() && structureStart.getBoundingBox().isInside(position)) {
                consumer.accept(structureStart);
            }
        }
    }

    public static int getMaxTerrainLimit(ChunkGenerator chunkGenerator) {
        return chunkGenerator.getMinY() + chunkGenerator.getGenDepth();
    }

    public static BlockPos getHighestLand(ChunkGenerator chunkGenerator, RandomState randomState, BoundingBox boundingBox, LevelHeightAccessor heightLimitView, boolean canBeOnLiquid) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(boundingBox.getCenter().getX(), getMaxTerrainLimit(chunkGenerator) - 40, boundingBox.getCenter().getZ());
        NoiseColumn blockView = chunkGenerator.getBaseColumn(mutable.getX(), mutable.getZ(), heightLimitView, randomState);
        BlockState currentBlockState;
        while (mutable.getY() > chunkGenerator.getSeaLevel()) {
            currentBlockState = blockView.getBlock(mutable.getY());
            if (!currentBlockState.canOcclude()) {
                mutable.move(Direction.DOWN);
                continue;
            } else if (blockView.getBlock(mutable.getY() + 3).isAir() && (canBeOnLiquid ? !currentBlockState.isAir() : currentBlockState.canOcclude())) {
                return mutable;
            }
            mutable.move(Direction.DOWN);
        }
        return mutable;
    }

    public static BlockPos getLowestLand(ChunkGenerator chunkGenerator, RandomState randomState, BoundingBox boundingBox, LevelHeightAccessor heightLimitView, boolean canBeOnLiquid) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(boundingBox.getCenter().getX(), chunkGenerator.getSeaLevel() + 1, boundingBox.getCenter().getZ());
        NoiseColumn blockView = chunkGenerator.getBaseColumn(mutable.getX(), mutable.getZ(), heightLimitView, randomState);
        BlockState currentBlockState = blockView.getBlock(mutable.getY());
        while (mutable.getY() <= getMaxTerrainLimit(chunkGenerator) - 40) {
            if ((canBeOnLiquid ? !currentBlockState.isAir() : currentBlockState.canOcclude())
                    && blockView.getBlock(mutable.getY() + 1).isAir()
                    && blockView.getBlock(mutable.getY() + 5).isAir()) {
                mutable.move(Direction.UP);
                return mutable;
            }
            mutable.move(Direction.UP);
            currentBlockState = blockView.getBlock(mutable.getY());
        }
        return mutable.set(mutable.getX(), chunkGenerator.getSeaLevel(), mutable.getZ());
    }

    public static int getCachedFreeHeight(ChunkGenerator chunkGenerator, int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        HeightKey key = new HeightKey(chunkGenerator, x, z);
        Integer y = CACHED_HEIGHT.get(key);
        if (y == null) {
            if (CACHED_HEIGHT.size() >= 2048) {
                CACHED_HEIGHT.clear();
            }
            y = chunkGenerator.getFirstFreeHeight(x, z, types, levelHeightAccessor, randomState);
            CACHED_HEIGHT.put(key, y);
        }
        return y;
    }

    private record HeightKey(ChunkGenerator chunkGenerator, int x, int z) {
    }
}
