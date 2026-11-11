package com.craisinlord.integrated_api.utils;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.storage.loot.LootTable;
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

    public static BlockState orientateChest(ServerLevelAccessor blockView, BlockPos blockPos, BlockState blockState) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        Direction wallDirection = blockState.getValue(HorizontalDirectionalBlock.FACING);

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            mutable.set(blockPos).move(facing);

            if (isFullCube(blockView, mutable, blockView.getBlockState(mutable))) {
                wallDirection = facing;

                mutable.move(facing.getOpposite(), 2);
                if (!blockView.getBlockState(mutable).isSolid()) {
                    break;
                }
            }
        }

        return blockState.setValue(HorizontalDirectionalBlock.FACING, wallDirection.getOpposite());
    }

    public static ItemStack enchantRandomly(RegistryAccess registryAccess, RandomSource random, ItemStack itemToEnchant, float chance) {
        if (random.nextFloat() < chance) {
            List<Holder.Reference<Enchantment>> list = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).listElements()
                    .filter(holder -> holder.value().canEnchant(itemToEnchant)).toList();
            if (!list.isEmpty()) {
                Holder.Reference<Enchantment> enchantment = list.get(random.nextInt(list.size()));
                int enchantmentLevel = random.nextInt(Mth.nextInt(random, enchantment.value().getMinLevel(), enchantment.value().getMaxLevel()) + 1);
                itemToEnchant.enchant(enchantment, enchantmentLevel);
            }
        }

        return itemToEnchant;
    }

    public static int getFirstLandYFromPos(LevelReader worldView, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        mutable.set(pos);
        ChunkAccess currentChunk = worldView.getChunk(mutable);
        BlockState currentState = currentChunk.getBlockState(mutable);

        while (mutable.getY() >= worldView.getMinY() && isReplaceableByStructures(currentState)) {
            mutable.move(Direction.DOWN);
            currentState = currentChunk.getBlockState(mutable);
        }

        return mutable.getY();
    }

    private static boolean isReplaceableByStructures(BlockState blockState) {
        return blockState.isAir() || !blockState.getFluidState().isEmpty() || blockState.is(BlockTags.REPLACEABLE_BY_TREES);
    }

    public static String getStringMicroOptimised(CompoundTag tag, String key) {
        return tag.get(key) instanceof StringTag stringTag ? stringTag.value() : "";
    }

    public static Map<Identifier, JsonElement> getDatapacksJSONElement(ResourceManager resourceManager, Gson gson, String dataType, int fileSuffixLength) {
        Map<Identifier, JsonElement> map = new HashMap<>();
        int dataTypeLength = dataType.length() + 1;

        for (Map.Entry<Identifier, Resource> resourceStackEntry : resourceManager.listResources(dataType, fileString -> true).entrySet()) {
            String identifierPath = resourceStackEntry.getKey().getPath();
            Identifier fileID = Identifier.fromNamespaceAndPath(
                    resourceStackEntry.getKey().getNamespace(),
                    identifierPath.substring(dataTypeLength, identifierPath.length() - fileSuffixLength));

            try {
                InputStream fileStream = resourceStackEntry.getValue().open();
                try (Reader bufferedReader = new BufferedReader(new InputStreamReader(fileStream, StandardCharsets.UTF_8))) {
                    JsonElement countsJSONElement = GsonHelper.fromJson(gson, bufferedReader, JsonElement.class);
                    map.put(fileID, countsJSONElement);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException exception) {
                IntegratedAPI.LOGGER.error(
                        "(Integrated API {} MERGER) Couldn't parse data file {} from {}",
                        dataType,
                        fileID,
                        resourceStackEntry,
                        exception);
            }
        }

        return map;
    }

    public static boolean isInvalidLootTableFound(MinecraftServer minecraftServer, Map.Entry<Identifier, Identifier> entry) {
        boolean invalidLootTableFound = false;
        HolderLookup.RegistryLookup<LootTable> lootTableRegistry = minecraftServer.reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE);
        if (isMissingLootTable(lootTableRegistry, entry.getKey())) {
            IntegratedAPI.LOGGER.error("Unable to find loot table key: {}", entry.getKey());
            invalidLootTableFound = true;
        }
        if (isMissingLootTable(lootTableRegistry, entry.getValue())) {
            IntegratedAPI.LOGGER.error("Unable to find loot table value: {}", entry.getValue());
            invalidLootTableFound = true;
        }
        return invalidLootTableFound;
    }

    private static boolean isMissingLootTable(HolderLookup.RegistryLookup<LootTable> lootTableRegistry, Identifier id) {
        Optional<Holder.Reference<LootTable>> lootTable = lootTableRegistry.get(ResourceKey.create(Registries.LOOT_TABLE, id));
        return lootTable.isEmpty() || lootTable.get().value() == LootTable.EMPTY;
    }

    public static boolean isMissingLootImporting(MinecraftServer minecraftServer, Set<Identifier> tableKeys) {
        AtomicBoolean invalidLootTableFound = new AtomicBoolean(false);
        HolderLookup.RegistryLookup<LootTable> lootTableRegistry = minecraftServer.reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE);
        lootTableRegistry.listElementIds().map(ResourceKey::identifier).forEach(rl -> {
            if (rl.getNamespace().equals(IntegratedAPI.MODID) && !tableKeys.contains(rl)) {
                if (rl.getPath().contains("mansions") && rl.getPath().contains("storage")) {
                    return;
                }

                if (rl.getPath().contains("monuments")) {
                    return;
                }

                if (rl.getPath().contains("dispensers/temples/wasteland_lava")) {
                    return;
                }

                if (rl.getPath().contains("lucky_pool")) {
                    return;
                }

                if (rl.getPath().contains("archaeology")) {
                    return;
                }

                IntegratedAPI.LOGGER.error("No loot importing found for: {}", rl);
                invalidLootTableFound.set(true);
            }
        });
        return invalidLootTableFound.get();
    }

    public static boolean nameMatch(String biomeName, String... targetMatch) {
        return Arrays.stream(targetMatch).anyMatch(biomeName::contains);
    }

    public static boolean nameExactMatch(String biomeName, String... targetMatch) {
        return Arrays.asList(targetMatch).contains(biomeName);
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

            StructureStart structureStart = structureManager.getStartForStructure(structure, level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_STARTS));
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
