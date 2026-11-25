package com.craisinlord.integrated_api.utils;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Map;
import java.util.function.Predicate;

public final class StructurePieceUtils {
    private StructurePieceUtils() {
    }

    /**
     * Whether the x/z position is within the given horizontal distance of any piece of a matching structure. Safe to call during worldgen.
     */
    public static boolean isNearPiece(WorldGenRegion level, BlockPos pos, int distance, Predicate<Structure> structureMatch) {
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_REFERENCES, false);
        if (chunk == null) {
            return false;
        }

        StructureManager structureManager = level.getLevel().structureManager();
        for (Map.Entry<Structure, LongSet> entry : chunk.getAllReferences().entrySet()) {
            if (entry.getValue().isEmpty() || !structureMatch.test(entry.getKey())) {
                continue;
            }

            for (long reference : entry.getValue()) {
                ChunkPos chunkPos = new ChunkPos(reference);
                if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                    continue;
                }

                StructureStart start = structureManager.getStartForStructure(SectionPos.bottomOf(level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS)), entry.getKey(), level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS));
                if (start == null || !start.isValid()) {
                    continue;
                }

                for (StructurePiece piece : start.getPieces()) {
                    BoundingBox box = piece.getBoundingBox();
                    if (pos.getX() >= box.minX() - distance && pos.getX() <= box.maxX() + distance
                            && pos.getZ() >= box.minZ() - distance && pos.getZ() <= box.maxZ() + distance) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
