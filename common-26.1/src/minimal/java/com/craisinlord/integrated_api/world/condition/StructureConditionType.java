package com.craisinlord.integrated_api.world.condition;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public interface StructureConditionType<C extends StructureCondition> {
    Map<Identifier, StructureConditionType<?>> CONDITION_TYPES_BY_NAME = new HashMap<>();
    Map<StructureConditionType<?>, Identifier> NAME_BY_CONDITION_TYPES = new HashMap<>();

    Codec<StructureConditionType<?>> CONDITION_TYPE_CODEC = Identifier.CODEC.flatXmap(
            identifier -> Optional.ofNullable(CONDITION_TYPES_BY_NAME.get(identifier))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown condition type: " + identifier)),
            conditionType -> Optional.of(NAME_BY_CONDITION_TYPES.get(conditionType))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "No ID found for condition type " + conditionType + ". Is it registered?")));

    Codec<StructureCondition> CONDITION_CODEC = CONDITION_TYPE_CODEC.dispatch("type", StructureCondition::type, StructureConditionType::codec);

    StructureConditionType<AlwaysTrueCondition> ALWAYS_TRUE = register("always_true", AlwaysTrueCondition.CODEC);
    StructureConditionType<AnyOfCondition> ANY_OF = register("any_of", AnyOfCondition.CODEC);
    StructureConditionType<AllOfCondition> ALL_OF = register("all_of", AllOfCondition.CODEC);
    StructureConditionType<NotCondition> NOT = register("not", NotCondition.CODEC);
    StructureConditionType<AltitudeCondition> ALTITUDE = register("altitude", AltitudeCondition.CODEC);
    StructureConditionType<DepthCondition> DEPTH = register("depth", DepthCondition.CODEC);
    StructureConditionType<RandomChanceCondition> RANDOM_CHANCE = register("random_chance", RandomChanceCondition.CODEC);
    StructureConditionType<PieceInRangeCondition> PIECE_IN_RANGE = register("piece_in_range", PieceInRangeCondition.CODEC);
    StructureConditionType<ModLoadedCondition> MOD_LOADED = register("mod_loaded", ModLoadedCondition.CODEC);
    StructureConditionType<PieceInHorizontalDirectionCondition> PIECE_IN_HORIZONTAL_DIRECTION = register("piece_in_horizontal_direction", PieceInHorizontalDirectionCondition.CODEC);
    StructureConditionType<RotationCondition> ROTATION = register("rotation", RotationCondition.CODEC);

    static <C extends StructureCondition> StructureConditionType<C> register(Identifier identifier, MapCodec<C> codec) {
        StructureConditionType<C> conditionType = () -> codec;
        CONDITION_TYPES_BY_NAME.put(identifier, conditionType);
        NAME_BY_CONDITION_TYPES.put(conditionType, identifier);
        return conditionType;
    }

    private static <C extends StructureCondition> StructureConditionType<C> register(String id, MapCodec<C> codec) {
        return register(Identifier.fromNamespaceAndPath(IntegratedAPI.MODID, id), codec);
    }

    MapCodec<C> codec();
}
