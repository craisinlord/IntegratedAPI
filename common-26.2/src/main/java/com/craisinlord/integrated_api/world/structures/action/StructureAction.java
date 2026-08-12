package com.craisinlord.integrated_api.world.structures.action;

import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.craisinlord.integrated_api.world.structures.pieces.assembler.PieceEntry;

/**
 * A serializable class used for modifying pieces of Jigsaw structures during world generation.
 */
public abstract class StructureAction {
    abstract public StructureActionType<?> type();
    abstract public void apply(StructureContext ctx, PieceEntry targetPieceEntry);
}