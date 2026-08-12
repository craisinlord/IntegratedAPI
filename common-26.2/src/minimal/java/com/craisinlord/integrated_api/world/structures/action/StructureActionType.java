package com.craisinlord.integrated_api.world.structures.action;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public interface StructureActionType<C extends StructureAction> {
    Map<Identifier, StructureActionType<?>> ACTION_TYPES_BY_NAME = new HashMap<>();
    Map<StructureActionType<?>, Identifier> NAME_BY_ACTION_TYPES = new HashMap<>();

    Codec<StructureActionType<?>> ACTION_TYPE_CODEC = Identifier.CODEC.flatXmap(
            identifier -> Optional.ofNullable(ACTION_TYPES_BY_NAME.get(identifier))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown structure action type: " + identifier)),
            actionType -> Optional.of(NAME_BY_ACTION_TYPES.get(actionType))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "No ID found for structure action type " + actionType + ". Is it registered?")));

    Codec<StructureAction> ACTION_CODEC = ACTION_TYPE_CODEC.dispatch("type", StructureAction::type, StructureActionType::codec);

    StructureActionType<TransformAction> TRANSFORM = register("transform", TransformAction.CODEC);
    StructureActionType<DelayGenerationAction> DELAY_GENERATION = register("delay_generation", DelayGenerationAction.CODEC);

    static <C extends StructureAction> StructureActionType<C> register(Identifier identifier, MapCodec<C> codec) {
        StructureActionType<C> actionType = () -> codec;
        ACTION_TYPES_BY_NAME.put(identifier, actionType);
        NAME_BY_ACTION_TYPES.put(actionType, identifier);
        return actionType;
    }

    private static <C extends StructureAction> StructureActionType<C> register(String id, MapCodec<C> codec) {
        return register(Identifier.fromNamespaceAndPath(IntegratedAPI.MODID, id), codec);
    }

    MapCodec<C> codec();
}
