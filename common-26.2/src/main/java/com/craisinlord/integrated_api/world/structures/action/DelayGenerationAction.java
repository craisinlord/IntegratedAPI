package com.craisinlord.integrated_api.world.structures.action;

import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.craisinlord.integrated_api.world.structures.pieces.assembler.PieceEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/**
 * Delays generation of target piece(s) to after the rest of the structure's pieces.
 */
public class DelayGenerationAction extends StructureAction {
    private static final DelayGenerationAction INSTANCE = new DelayGenerationAction();
    public static final MapCodec<DelayGenerationAction> CODEC = MapCodec.unit(() -> INSTANCE);

    public DelayGenerationAction() {
    }

    @Override
    public StructureActionType<?> type() {
        return StructureActionType.DELAY_GENERATION;
    }

    @Override
    public void apply(StructureContext ctx, PieceEntry targetPieceEntry) {
        targetPieceEntry.setDelayGeneration(true);
    }
}