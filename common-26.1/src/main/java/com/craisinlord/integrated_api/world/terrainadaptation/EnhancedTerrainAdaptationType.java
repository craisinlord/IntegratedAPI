package com.craisinlord.integrated_api.world.terrainadaptation;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Specifies the type of a specific {@link EnhancedTerrainAdaptation}.
 * This class also serves as the registration hub for EnhancedTerrainAdaptations and their corresponding types.
 */
public interface EnhancedTerrainAdaptationType<C extends EnhancedTerrainAdaptation> {
    /* Utility maps for codecs. Simulates the approach vanilla registries use. */
    Map<Identifier, EnhancedTerrainAdaptationType<?>> ADAPTATION_TYPES_BY_NAME = new HashMap<>();
    Map<EnhancedTerrainAdaptationType<?>, Identifier> NAME_BY_ADAPTATION_TYPES = new HashMap<>();

    /* Codecs */
    Codec<EnhancedTerrainAdaptationType<?>> ADAPTATION_TYPE_CODEC = Identifier.CODEC
            .flatXmap(
                    Identifier -> Optional.ofNullable(ADAPTATION_TYPES_BY_NAME.get(Identifier))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "Unknown enhanced terrain adaptation type: " + Identifier)),
                    adaptationType -> Optional.of(NAME_BY_ADAPTATION_TYPES.get(adaptationType))
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "No ID found for enhanced terrain adaptation type " + adaptationType + ". Is it registered?")));

    Codec<EnhancedTerrainAdaptation> ADAPTATION_CODEC = ADAPTATION_TYPE_CODEC
            .dispatch("type", EnhancedTerrainAdaptation::type, EnhancedTerrainAdaptationType::codec);

    /* Types. Add any new types here! */
    EnhancedTerrainAdaptationType<NoneAdaptation> NONE = register("none", NoneAdaptation.CODEC);
    EnhancedTerrainAdaptationType<LargeCarvedTopNoBeardAdaptation> LARGE_CARVED_TOP_NO_BEARD =
            register("carved_top_no_beard_large", LargeCarvedTopNoBeardAdaptation.CODEC);
    EnhancedTerrainAdaptationType<SmallCarvedTopNoBeardAdaptation> SMALL_CARVED_TOP_NO_BEARD =
            register("carved_top_no_beard_small", SmallCarvedTopNoBeardAdaptation.CODEC);
    EnhancedTerrainAdaptationType<CustomAdaptation> CUSTOM = register("custom", CustomAdaptation.CODEC);

    /**
     * Utility method for registering EnhancedTerrainAdaptationTypes.
     */
    static <C extends EnhancedTerrainAdaptation> EnhancedTerrainAdaptationType<C> register(Identifier Identifier, MapCodec<C> codec) {
        EnhancedTerrainAdaptationType<C> adaptationType = () -> codec;
        ADAPTATION_TYPES_BY_NAME.put(Identifier, adaptationType);
        NAME_BY_ADAPTATION_TYPES.put(adaptationType, Identifier);
        return adaptationType;
    }

    /**
     * Private utility method for registering EnhancedTerrainAdaptationTypes native to YUNG's API.
     */
    private static <C extends EnhancedTerrainAdaptation> EnhancedTerrainAdaptationType<C> register(String id, MapCodec<C> codec) {
        return register(Identifier.fromNamespaceAndPath(IntegratedAPI.MODID, id), codec);
    }

    /**
     * Supplies the codec for the {@link EnhancedTerrainAdaptation} corresponding to this EnhancedTerrainAdaptationType.
     */
    MapCodec<C> codec();
}