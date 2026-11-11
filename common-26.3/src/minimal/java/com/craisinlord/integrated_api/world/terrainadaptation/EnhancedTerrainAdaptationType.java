package com.craisinlord.integrated_api.world.terrainadaptation;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public interface EnhancedTerrainAdaptationType<C extends EnhancedTerrainAdaptation> {
    Map<Identifier, EnhancedTerrainAdaptationType<?>> ADAPTATION_TYPES_BY_NAME = new HashMap<>();
    Map<EnhancedTerrainAdaptationType<?>, Identifier> NAME_BY_ADAPTATION_TYPES = new HashMap<>();

    Codec<EnhancedTerrainAdaptationType<?>> ADAPTATION_TYPE_CODEC = Identifier.CODEC.flatXmap(
            identifier -> Optional.ofNullable(ADAPTATION_TYPES_BY_NAME.get(identifier))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown enhanced terrain adaptation type: " + identifier)),
            adaptationType -> Optional.of(NAME_BY_ADAPTATION_TYPES.get(adaptationType))
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "No ID found for enhanced terrain adaptation type " + adaptationType + ". Is it registered?")));

    Codec<EnhancedTerrainAdaptation> ADAPTATION_CODEC = ADAPTATION_TYPE_CODEC.dispatch("type", EnhancedTerrainAdaptation::type, EnhancedTerrainAdaptationType::codec);

    EnhancedTerrainAdaptationType<NoneAdaptation> NONE = register("none", NoneAdaptation.CODEC);
    EnhancedTerrainAdaptationType<LargeCarvedTopNoBeardAdaptation> LARGE_CARVED_TOP_NO_BEARD =
            register("carved_top_no_beard_large", LargeCarvedTopNoBeardAdaptation.CODEC);
    EnhancedTerrainAdaptationType<SmallCarvedTopNoBeardAdaptation> SMALL_CARVED_TOP_NO_BEARD =
            register("carved_top_no_beard_small", SmallCarvedTopNoBeardAdaptation.CODEC);
    EnhancedTerrainAdaptationType<CustomAdaptation> CUSTOM = register("custom", CustomAdaptation.CODEC);

    static <C extends EnhancedTerrainAdaptation> EnhancedTerrainAdaptationType<C> register(Identifier identifier, MapCodec<C> codec) {
        EnhancedTerrainAdaptationType<C> adaptationType = () -> codec;
        ADAPTATION_TYPES_BY_NAME.put(identifier, adaptationType);
        NAME_BY_ADAPTATION_TYPES.put(adaptationType, identifier);
        return adaptationType;
    }

    private static <C extends EnhancedTerrainAdaptation> EnhancedTerrainAdaptationType<C> register(String id, MapCodec<C> codec) {
        return register(Identifier.fromNamespaceAndPath(IntegratedAPI.MODID, id), codec);
    }

    MapCodec<C> codec();
}
