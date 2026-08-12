package com.craisinlord.integrated_api.world.terrainadaptation.beardifier;

import com.craisinlord.integrated_api.world.structures.JigsawStructure;
import com.craisinlord.integrated_api.world.structures.pieces.IAPoolElement;
import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.List;

/**
 * A collection of static helper methods intended to be used by Beardifier Mixin.
 */
public class EnhancedBeardifierHelper {
    /**
     * Attaches additional behavior for an Enhanced Beardifier, which is used for {@link EnhancedTerrainAdaptation}s.
     * @param structureManager The StructureManager
     * @param chunkPos The ChunkPos
     * @param original The original vanilla Beardifier, created by {@link Beardifier#forStructuresInChunk(StructureManager, ChunkPos)}
     * @return The original Beardifier vanilla Beardifier, with additional data for Enhanced behaviors
     */
    public static Beardifier forStructuresInChunk(StructureManager structureManager, ChunkPos chunkPos, Beardifier original) {
        ObjectList<EnhancedBeardifierRigid> enhancedBeardifierRigidList = new ObjectArrayList<>(10);
        ObjectList<EnhancedJigsawJunction> enhancedJunctionList = new ObjectArrayList<>(10);
        int chunkMinBlockX = chunkPos.getMinBlockX();
        int chunkMinBlockZ = chunkPos.getMinBlockZ();
        List<StructureStart> structureStarts = structureManager.startsForStructure(chunkPos, structure -> structure instanceof JigsawStructure);
        for (StructureStart structureStart : structureStarts) {
            EnhancedTerrainAdaptation structureTerrainAdaptation = ((JigsawStructure) structureStart.getStructure()).enhancedTerrainAdaptation;

            // Determine max kernel radius in the structure.
            // Both the structure itself and any pieces may specify an enhanced terrain adaptation, so we must
            // check everything.
            int kernelRadius = structureTerrainAdaptation.getKernelRadius();
            for (StructurePiece structurePiece : structureStart.getPieces()) {
                if (structurePiece instanceof PoolElementStructurePiece poolPiece
                        && poolPiece.getElement() instanceof IAPoolElement iaElement
                        && iaElement.getEnhancedTerrainAdaptation().isPresent()) {
                    kernelRadius = Math.max(kernelRadius, iaElement.getEnhancedTerrainAdaptation().get().getKernelRadius());
                }
            }

            int maxKernelRadius = kernelRadius;
            if (maxKernelRadius <= 0) {
                continue;
            }

            // Use max kernel radius to get list of nearby pieces for this chunk.
            // A piece is considered nearby if its bounding box, when padded by maxKernelRadius, intersects this chunk.
            List<StructurePiece> nearbyPieces = structureStart.getPieces().stream()
                    .filter(structurePiece -> structurePiece.isCloseToChunk(chunkPos, maxKernelRadius))
                    .toList();

            for (StructurePiece nearbyPiece : nearbyPieces) {
                if (nearbyPiece instanceof PoolElementStructurePiece poolElementPiece) {
                    StructureTemplatePool.Projection projection = poolElementPiece.getElement().getProjection();

                    // Check if piece overrides terrain adaptation
                    EnhancedTerrainAdaptation pieceTerrainAdaptation = structureTerrainAdaptation;
                    if (poolElementPiece.getElement() instanceof IAPoolElement iaElement && iaElement.getEnhancedTerrainAdaptation().isPresent()) {
                        pieceTerrainAdaptation = iaElement.getEnhancedTerrainAdaptation().get();
                    }

                    // If no terrain adaptation for this piece, we can ignore it
                    if (pieceTerrainAdaptation == EnhancedTerrainAdaptation.NONE) continue;

                    int pieceKernelRadius = pieceTerrainAdaptation.getKernelRadius();


                    // Add rigid for piece
                    if (projection == StructureTemplatePool.Projection.RIGID) {
                        enhancedBeardifierRigidList.add(
                                new EnhancedBeardifierRigid(
                                        poolElementPiece.getBoundingBox(),
//                                        new BoundingBox(poolElementPiece.getBoundingBox().maxX(), poolElementPiece.getBoundingBox().maxY(), poolElementPiece.getBoundingBox().maxZ(), poolElementPiece.getBoundingBox().minX(), poolElementPiece.getBoundingBox().minY() + 1, poolElementPiece.getBoundingBox().minZ()),
                                        pieceTerrainAdaptation,
                                        poolElementPiece.getGroundLevelDelta()
                                )
                        );
                    }

                    // Add rigid for jigsaw junctions within the intersecting piece
                    for (JigsawJunction jigsawJunction : poolElementPiece.getJunctions()) {
                        int sourceX = jigsawJunction.getSourceX();
                        int sourceZ = jigsawJunction.getSourceZ();
                        // Only consider junctions which are intersecting with this chunk (padded by kernel radius)
                        if (sourceX > chunkMinBlockX - pieceKernelRadius
                                && sourceZ > chunkMinBlockZ - pieceKernelRadius
                                && sourceX < chunkMinBlockX + 15 + pieceKernelRadius
                                && sourceZ < chunkMinBlockZ + 15 + pieceKernelRadius) {
                            enhancedJunctionList.add(new EnhancedJigsawJunction(jigsawJunction, pieceTerrainAdaptation));
                        }
                    }
                } else if (structureTerrainAdaptation != EnhancedTerrainAdaptation.NONE) {
                    enhancedBeardifierRigidList.add(new EnhancedBeardifierRigid(
                            nearbyPiece.getBoundingBox(),
                            structureTerrainAdaptation,
                            0));
                }
            }
        }

        ((EnhancedBeardifierData) original).setEnhancedRigidIterator(enhancedBeardifierRigidList.iterator());
        ((EnhancedBeardifierData) original).setEnhancedJunctionIterator(enhancedJunctionList.iterator());
        return original;
    }

    /**
     * Computes the updated density value at the given point, accounting for noise contributions from the EnhancedBeardifierData.
     * @param ctx the density FunctionContext
     * @param density The originally computed vanilla density value at this position
     * @param data The {@link EnhancedBeardifierData} to be used in the computation of the new density value.
     * @return The new density value at the given location, accounting for additional noise contributions.
     */
    public static double computeDensity(DensityFunction.FunctionContext ctx, double density, EnhancedBeardifierData data) {
        int x = ctx.blockX();
        int y = ctx.blockY();
        int z = ctx.blockZ();

        var rigidIterator = data.getEnhancedRigidIterator();
        while (rigidIterator != null && rigidIterator.hasNext()) {
            EnhancedBeardifierRigid rigid = rigidIterator.next();
            BoundingBox pieceBoundingBox = rigid.pieceBoundingBox();
            EnhancedTerrainAdaptation pieceTerrainAdaptation = rigid.pieceTerrainAdaptation();
            boolean inverted = pieceTerrainAdaptation.direction().isInverted();
            int beardBaseY = inverted ? pieceBoundingBox.maxY() : pieceBoundingBox.minY();

            /* Get the distance from the pieceBoundingBox along each axis.
             * If within the bounding box, all of these are simply 0.
             * Notably, the below equations grab the maximum *positive* distance. I'm not sure why,
             * as it seems a negative distance value would also work in the call to computeDensityFactor.
             * I don't know, I'm just recreating vanilla logic here.
             */
            int xDistanceToBoundingBox = Math.max(0, Math.max(pieceBoundingBox.minX() - x, x - pieceBoundingBox.maxX()));
            int yDistanceToBoundingBox = Math.max(0, Math.max(pieceBoundingBox.minY() - y, y - pieceBoundingBox.maxY()));
            int zDistanceToBoundingBox = Math.max(0, Math.max(pieceBoundingBox.minZ() - z, z - pieceBoundingBox.maxZ()));
            int yDistanceToBeardBase = inverted ? beardBaseY - y : y - beardBaseY;

            // Calculate density factor and add to density value
            double densityFactor = 0;
            if (pieceTerrainAdaptation != EnhancedTerrainAdaptation.NONE) {
                densityFactor = pieceTerrainAdaptation.computeDensityFactor(
                        xDistanceToBoundingBox,
                        yDistanceToBoundingBox,
                        zDistanceToBoundingBox,
                        yDistanceToBeardBase
                ) * 0.8D;
            }

            density += densityFactor;
        }
        if (rigidIterator != null) {
            rigidIterator.back(Integer.MAX_VALUE);
        }

        // Vanilla logic
        var junctionIterator = data.getEnhancedJunctionIterator();
        while (junctionIterator != null && junctionIterator.hasNext()) {
            EnhancedJigsawJunction enhancedJigsawJunction = junctionIterator.next();
            JigsawJunction jigsawJunction = enhancedJigsawJunction.jigsawJunction();
            EnhancedTerrainAdaptation pieceTerrainAdaptation = enhancedJigsawJunction.pieceTerrainAdaptation();
            int xDistanceToJunction = x - jigsawJunction.getSourceX();
            int yDistanceToJunction = pieceTerrainAdaptation.direction().isInverted()
                    ? jigsawJunction.getSourceGroundY() - y
                    : y - jigsawJunction.getSourceGroundY();
            int zDistanceToJunction = z - jigsawJunction.getSourceZ();
            density += pieceTerrainAdaptation.computeDensityFactor(
                    xDistanceToJunction,
                    yDistanceToJunction,
                    zDistanceToJunction,
                    yDistanceToJunction
            ) * 0.4D;
        }
        if (junctionIterator != null) {
            junctionIterator.back(Integer.MAX_VALUE);
        }

        return density;
    }
}
