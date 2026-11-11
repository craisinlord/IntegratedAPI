package com.craisinlord.integrated_api.world.structures.modifier;

import com.craisinlord.integrated_api.world.condition.StructureCondition;
import com.craisinlord.integrated_api.world.condition.StructureConditionType;
import com.craisinlord.integrated_api.world.structures.action.StructureAction;
import com.craisinlord.integrated_api.world.structures.action.StructureActionType;
import com.craisinlord.integrated_api.world.structures.context.StructureContext;
import com.craisinlord.integrated_api.world.structures.pieces.assembler.PieceEntry;
import com.craisinlord.integrated_api.world.structures.targetselector.StructureTargetSelector;
import com.craisinlord.integrated_api.world.structures.targetselector.StructureTargetSelectorType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Applies a list of Structure Actions to target(s) determined by an associated StructureTargetSelector,
 * only if an associated {@link StructureCondition} passes.
 * This is really just a convenience wrapper for associating actions, target selectors, and conditions;
 * thereby easing JSON (de)serialization.
 */
public class StructureModifier {
    public static final Codec<StructureModifier> CODEC = RecordCodecBuilder.create((builder) -> builder
            .group(
                    StructureConditionType.CONDITION_CODEC.fieldOf("condition").forGetter(modifier -> modifier.condition),
                    StructureActionType.ACTION_CODEC.listOf().fieldOf("actions").forGetter(modifier -> modifier.actions),
                    StructureTargetSelectorType.TARGET_SELECTOR_CODEC.fieldOf("target_selector").forGetter(modifier -> modifier.targetSelector))
            .apply(builder, StructureModifier::new));

    private final StructureCondition condition;
    private final List<StructureAction> actions;
    private final StructureTargetSelector targetSelector;

    public StructureModifier(StructureCondition condition, List<StructureAction> actions, StructureTargetSelector targetSelector) {
        this.condition = condition;
        this.actions = actions;
        this.targetSelector = targetSelector;
    }

    public boolean apply(StructureContext structureContext) {
        // Validate condition
        if (!this.condition.passes(structureContext)) {
            return false;
        }

        // Validate target
        List<PieceEntry> targets = this.targetSelector.apply(structureContext);

        // Apply actions to each target
        for (PieceEntry target : targets) {
            this.actions.forEach(action -> action.apply(structureContext, target));
        }

        return true;
    }
}