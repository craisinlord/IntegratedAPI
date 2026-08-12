package com.craisinlord.integrated_api.world.structures.context;

import com.craisinlord.integrated_api.world.structures.pieces.assembler.PieceEntry;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class StructureContext {
    private final int pieceMinY;
    private final int pieceMaxY;
    private final int depth;
    private final BlockPos pos;
    private final Rotation rotation;
    private final StructureTemplateManager structureTemplateManager;
    private final List<PieceEntry> pieces;
    private final PieceEntry pieceEntry;
    private final RandomSource random;

    private StructureContext(Builder builder) {
        this.pieceMinY = builder.pieceMinY;
        this.pieceMaxY = builder.pieceMaxY;
        this.depth = builder.depth;
        this.pos = builder.pos;
        this.structureTemplateManager = builder.structureTemplateManager;
        this.pieces = builder.pieces;
        this.pieceEntry = builder.pieceEntry;
        this.rotation = builder.rotation;
        this.random = builder.random;
    }

    public int pieceMinY() {
        return pieceMinY;
    }

    public int pieceMaxY() {
        return pieceMaxY;
    }

    public int depth() {
        return depth;
    }

    public BlockPos pos() {
        return pos;
    }

    public Rotation rotation() {
        return rotation;
    }

    public StructureTemplateManager structureTemplateManager() {
        return structureTemplateManager;
    }

    public List<PieceEntry> pieces() {
        return pieces;
    }

    public PieceEntry pieceEntry() {
        return this.pieceEntry;
    }

    public RandomSource random() {
        return this.random;
    }

    public static class Builder {
        private int pieceMinY;
        private int pieceMaxY;
        private int depth;
        private BlockPos pos;
        private Rotation rotation;
        private StructureTemplateManager structureTemplateManager;
        private List<PieceEntry> pieces;
        private PieceEntry pieceEntry;
        private RandomSource random;

        public Builder pieceMinY(int pieceMinY) {
            this.pieceMinY = pieceMinY;
            return this;
        }

        public Builder pieceMaxY(int pieceMaxY) {
            this.pieceMaxY = pieceMaxY;
            return this;
        }

        public Builder depth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder pos(BlockPos pos) {
            this.pos = pos;
            return this;
        }

        public Builder rotation(Rotation rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder structureTemplateManager(StructureTemplateManager structureTemplateManager) {
            this.structureTemplateManager = structureTemplateManager;
            return this;
        }

        public Builder pieces(List<PieceEntry> pieces) {
            this.pieces = pieces;
            return this;
        }

        public Builder pieceEntry(PieceEntry pieceEntry) {
            this.pieceEntry = pieceEntry;
            return this;
        }

        public Builder random(RandomSource random) {
            this.random = random;
            return this;
        }

        public StructureContext build() {
            return new StructureContext(this);
        }
    }
}
