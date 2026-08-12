package com.craisinlord.integrated_api.world.structures.targetselector;

import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.craisinlord.integrated_api.world.structures.pieces.assembler.PieceEntry;

import java.util.List;

/**
 * A serializable class used for selecting a list of {@link PieceEntry}s during world generation.
 */
public abstract class StructureTargetSelector {
    abstract public StructureTargetSelectorType<?> type();
    abstract public List<PieceEntry> apply(StructureContext ctx);
}