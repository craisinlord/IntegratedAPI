package com.craisinlord.integrated_api.world.condition;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Specifies the type of a specific {@link StructureCondition}.
 * This class also serves as the registration hub for StructureConditions and their corresponding types.
 */
public interface StructureConditionType<C extends StructureCondition> {
    /* Utility maps for codecs. Simulates the approach vanilla registries use. */
    Map<Identifier, StructureConditionType<?>> CONDITION_TYPES_BY_NAME = new HashMap<>();
    Map<StructureConditionType<?>, Identifier> NAME_BY_CONDITION_TYPES = new HashMap<>();

    /* Codecs */
    Codec<StructureConditionType<?>> CONDITION_TYPE_CODEC = Identifier.CODEC
            .flatXmap(
                    Identifier -> Optional.ofNullable(CONDITION_TYPES_BY_NAME.get(Identifier))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "Unknown condition type: " + Identifier)),
                    conditionType -> Optional.of(NAME_BY_CONDITION_TYPES.get(conditionType))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "No ID found for condition type " + conditionType + ". Is it registered?")));

    Codec<StructureCondition> CONDITION_CODEC = CONDITION_TYPE_CODEC
            .dispatch("type", StructureCondition::type, StructureConditionType::codec);

    /* Types. Add any new types here! */
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

    /**
     * Utility method for registering StructureConditionTypes.
     */
    static <C extends StructureCondition> StructureConditionType<C> register(Identifier Identifier, MapCodec<C> codec) {
        StructureConditionType<C> conditionType = () -> codec;
        CONDITION_TYPES_BY_NAME.put(Identifier, conditionType);
        NAME_BY_CONDITION_TYPES.put(conditionType, Identifier);
        return conditionType;
    }

    /**
     * Private utility method for registering StructureConditionTypes
     */
    private static <C extends StructureCondition> StructureConditionType<C> register(String id, MapCodec<C> codec) {
        return register(Identifier.fromNamespaceAndPath(IntegratedAPI.MODID, id), codec);
    }

    /**
     * Supplies the codec for the {@link StructureCondition} corresponding to this StructureConditionType.
     */
    MapCodec<C> codec();
}