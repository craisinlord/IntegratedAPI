package com.craisinlord.integrated_api.world.condition;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.craisinlord.integrated_api.world.structures.pieces.assembler.PieceEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;

public class PieceInRangeCondition extends StructureCondition {
    private static final Identifier ALL = Identifier.fromNamespaceAndPath(IntegratedAPI.MODID, "all");
    public static final MapCodec<PieceInRangeCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Identifier.CODEC.listOf().optionalFieldOf("pieces", new ArrayList<>()).forGetter(condition -> condition.matchPieces),
            Codec.INT.optionalFieldOf("above_range", 0).forGetter(condition -> condition.aboveRange),
            Codec.INT.optionalFieldOf("horizontal_range", 0).forGetter(condition -> condition.horizontalRange),
            Codec.INT.optionalFieldOf("below_range", 0).forGetter(condition -> condition.belowRange)
    ).apply(builder, PieceInRangeCondition::new));

    private final List<Identifier> matchPieces;
    private final Integer aboveRange;
    private final Integer horizontalRange;
    private final Integer belowRange;

    public PieceInRangeCondition(List<Identifier> pieces, int aboveRange, int horizontalRange, int belowRange) {
        this.matchPieces = pieces;
        this.aboveRange = aboveRange;
        this.horizontalRange = horizontalRange;
        this.belowRange = belowRange;
        if (matchPieces.isEmpty()) {
            matchPieces.add(ALL);
        }
    }

    @Override
    public StructureConditionType<?> type() {
        return StructureConditionType.PIECE_IN_RANGE;
    }

    @Override
    public boolean passes(StructureContext ctx) {
        List<PieceEntry> pieces = ctx.pieces();
        PieceEntry pieceEntry = ctx.pieceEntry();
        if (pieces == null) {
            IntegratedAPI.LOGGER.error("Missing required field 'pieces' for piece_in_range condition!");
        }
        if (pieceEntry == null) {
            IntegratedAPI.LOGGER.error("Missing required field 'pieceEntry' for piece_in_range condition!");
        }
        if (pieces == null || pieceEntry == null) {
            return false;
        }

        PoolElementStructurePiece piece = pieceEntry.getPiece();
        BoundingBox searchBox = new BoundingBox(
                piece.getBoundingBox().minX() - this.horizontalRange,
                piece.getBoundingBox().minY() - this.belowRange,
                piece.getBoundingBox().minZ() - this.horizontalRange,
                piece.getBoundingBox().maxX() + this.horizontalRange,
                piece.getBoundingBox().maxY() + this.aboveRange,
                piece.getBoundingBox().maxZ() + this.horizontalRange);

        for (PieceEntry otherPieceEntry : pieces) {
            PoolElementStructurePiece otherPiece = otherPieceEntry.getPiece();
            if (otherPiece.getBoundingBox().equals(piece.getBoundingBox())) {
                continue;
            }
            Identifier otherTemplateId = otherPieceEntry.getTemplateLocation();
            for (Identifier matchPieceId : matchPieces) {
                if ((matchPieceId.equals(ALL) || otherTemplateId.equals(matchPieceId))
                        && otherPiece.getBoundingBox().intersects(searchBox)
                        && !otherPiece.getBoundingBox().intersects(piece.getBoundingBox())) {
                    return true;
                }
            }
        }
        return false;
    }
}
