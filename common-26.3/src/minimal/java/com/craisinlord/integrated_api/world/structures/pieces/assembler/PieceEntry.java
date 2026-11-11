package com.craisinlord.integrated_api.world.structures.pieces.assembler;

import com.craisinlord.integrated_api.utils.BoxOctree;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableObject;

public class PieceEntry {
    private PoolElementStructurePiece piece;
    private final MutableObject<BoxOctree> boxOctree;
    private final AABB pieceAabb;
    private final int depth;
    private boolean delayGeneration;

    public PieceEntry(PoolElementStructurePiece piece, MutableObject<BoxOctree> boxOctree, AABB pieceAabb, int depth) {
        this.piece = piece;
        this.boxOctree = boxOctree;
        this.pieceAabb = pieceAabb;
        this.depth = depth;
    }

    public PoolElementStructurePiece getPiece() {
        return piece;
    }

    public void setPiece(PoolElementStructurePiece newPiece) {
        this.piece = newPiece;
    }

    public MutableObject<BoxOctree> getBoxOctree() {
        return boxOctree;
    }

    public AABB getPieceAabb() {
        return pieceAabb;
    }

    public int getDepth() {
        return depth;
    }

    public void setDelayGeneration(boolean delayGeneration) {
        this.delayGeneration = delayGeneration;
    }

    public boolean isDelayGeneration() {
        return this.delayGeneration;
    }

    public Identifier getTemplateLocation() {
        if (this.piece.getElement() instanceof com.craisinlord.integrated_api.world.structures.pieces.IASinglePoolElement iaSinglePoolElement) {
            return iaSinglePoolElement.getTemplateLocation();
        }
        if (this.piece.getElement() instanceof net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement singlePoolElement) {
            return singlePoolElement.getTemplateLocation();
        }
        return Identifier.fromNamespaceAndPath("minecraft", "empty");
    }
}
