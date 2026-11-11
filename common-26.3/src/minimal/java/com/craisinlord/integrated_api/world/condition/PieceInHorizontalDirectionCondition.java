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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;

public class PieceInHorizontalDirectionCondition extends StructureCondition {
    private static final Identifier ALL = Identifier.fromNamespaceAndPath(IntegratedAPI.MODID, "all");
    public static final MapCodec<PieceInHorizontalDirectionCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Identifier.CODEC.listOf().optionalFieldOf("pieces", new ArrayList<>()).forGetter(condition -> condition.matchPieces),
            Codec.INT.fieldOf("range").forGetter(condition -> condition.range),
            Rotation.CODEC.fieldOf("rotation").forGetter(condition -> condition.rotation)
    ).apply(builder, PieceInHorizontalDirectionCondition::new));

    private final List<Identifier> matchPieces;
    private final Integer range;
    private final Rotation rotation;

    public PieceInHorizontalDirectionCondition(List<Identifier> pieces, int range, Rotation rotation) {
        this.matchPieces = pieces;
        this.range = range;
        this.rotation = rotation;
        if (matchPieces.isEmpty()) {
            matchPieces.add(ALL);
        }
    }

    @Override
    public StructureConditionType<?> type() {
        return StructureConditionType.PIECE_IN_HORIZONTAL_DIRECTION;
    }

    @Override
    public boolean passes(StructureContext ctx) {
        List<PieceEntry> pieces = ctx.pieces();
        Rotation pieceRotation = ctx.rotation();
        PieceEntry pieceEntry = ctx.pieceEntry();
        if (pieces == null) {
            IntegratedAPI.LOGGER.error("Missing required field 'pieces' for piece_in_horizontal_direction condition!");
        }
        if (rotation == null) {
            IntegratedAPI.LOGGER.error("Missing required field 'rotation' for piece_in_horizontal_direction condition!");
        }
        if (pieceEntry == null) {
            IntegratedAPI.LOGGER.error("Missing required field 'pieceEntry' for piece_in_horizontal_direction condition!");
        }
        if (pieces == null || rotation == null || pieceEntry == null || pieceRotation == null) {
            return false;
        }

        PoolElementStructurePiece piece = pieceEntry.getPiece();
        Rotation searchRotation = pieceRotation.getRotated(this.rotation);
        int negX = 0;
        int negZ = 0;
        int posX = 0;
        int posZ = 0;
        switch (searchRotation) {
            case NONE -> negZ = this.range;
            case CLOCKWISE_90 -> posX = this.range;
            case CLOCKWISE_180 -> posZ = this.range;
            case COUNTERCLOCKWISE_90 -> negX = this.range;
        }

        BoundingBox searchBox = new BoundingBox(
                piece.getBoundingBox().minX() - negX,
                piece.getBoundingBox().minY(),
                piece.getBoundingBox().minZ() - negZ,
                piece.getBoundingBox().maxX() + posX,
                piece.getBoundingBox().maxY(),
                piece.getBoundingBox().maxZ() + posZ);

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
