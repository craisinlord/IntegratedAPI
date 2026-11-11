package com.craisinlord.integrated_api.world.placements;

import com.craisinlord.integrated_api.modinit.IAPlacements;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class MinDistanceFromWorldOriginPlacement extends PlacementModifier {
    public static final MapCodec<MinDistanceFromWorldOriginPlacement> CODEC = ExtraCodecs.NON_NEGATIVE_INT.fieldOf("min_distance_from_world_origin").xmap(MinDistanceFromWorldOriginPlacement::new, countPlacement -> countPlacement.minDistanceFromWorldOrigin);
    private final int minDistanceFromWorldOrigin;

    private MinDistanceFromWorldOriginPlacement(int intProvider) {
        this.minDistanceFromWorldOrigin = intProvider;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
        return blockPos.distManhattan(BlockPos.ZERO) > minDistanceFromWorldOrigin ? Stream.of(blockPos) : Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return IAPlacements.MIN_DISTANCE_FROM_WORLD_ORIGIN_PLACEMENT.get();
    }
}
