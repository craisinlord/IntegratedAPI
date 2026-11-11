package com.craisinlord.integrated_api.world.structures.targetselector;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public interface StructureTargetSelectorType<C extends StructureTargetSelector> {
    Map<Identifier, StructureTargetSelectorType<?>> TARGET_SELECTOR_TYPES_BY_NAME = new HashMap<>();
    Map<StructureTargetSelectorType<?>, Identifier> NAME_BY_TARGET_SELECTOR_TYPES = new HashMap<>();

    Codec<StructureTargetSelectorType<?>> TARGET_SELECTOR_TYPE_CODEC = Identifier.CODEC.flatXmap(
            identifier -> Optional.ofNullable(TARGET_SELECTOR_TYPES_BY_NAME.get(identifier))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown target selector type: " + identifier)),
            targetSelectorType -> Optional.of(NAME_BY_TARGET_SELECTOR_TYPES.get(targetSelectorType))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "No ID found for target selector type " + targetSelectorType + ". Is it registered?")));

    Codec<StructureTargetSelector> TARGET_SELECTOR_CODEC = TARGET_SELECTOR_TYPE_CODEC.dispatch("type", StructureTargetSelector::type, StructureTargetSelectorType::codec);

    StructureTargetSelectorType<SelfTargetSelector> SELF = register("self", SelfTargetSelector.CODEC);

    static <C extends StructureTargetSelector> StructureTargetSelectorType<C> register(Identifier identifier, MapCodec<C> codec) {
        StructureTargetSelectorType<C> targetSelectorType = () -> codec;
        TARGET_SELECTOR_TYPES_BY_NAME.put(identifier, targetSelectorType);
        NAME_BY_TARGET_SELECTOR_TYPES.put(targetSelectorType, identifier);
        return targetSelectorType;
    }

    private static <C extends StructureTargetSelector> StructureTargetSelectorType<C> register(String id, MapCodec<C> codec) {
        return register(Identifier.fromNamespaceAndPath(IntegratedAPI.MODID, id), codec);
    }

    MapCodec<C> codec();
}
