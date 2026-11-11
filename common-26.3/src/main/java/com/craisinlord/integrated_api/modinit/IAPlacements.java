package com.craisinlord.integrated_api.modinit;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.registry.RegistryEntry;
import com.craisinlord.integrated_api.modinit.registry.ResourcefulRegistries;
import com.craisinlord.integrated_api.modinit.registry.ResourcefulRegistry;
import com.craisinlord.integrated_api.world.placements.MinDistanceFromWorldOriginPlacement;
import com.craisinlord.integrated_api.world.placements.MinusEightPlacement;
import com.craisinlord.integrated_api.world.placements.SnapToLowerNonAirPlacement;
import com.craisinlord.integrated_api.world.placements.UnlimitedCountPlacement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class IAPlacements {
	public static final ResourcefulRegistry<PlacementModifierType<?>> PLACEMENT_MODIFIER = ResourcefulRegistries.create(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, IntegratedAPI.MODID);

	public static final RegistryEntry<PlacementModifierType<MinusEightPlacement>> MINUS_EIGHT_PLACEMENT = PLACEMENT_MODIFIER.register("minus_eight_placement", () -> () -> MinusEightPlacement.CODEC);
	public static final RegistryEntry<PlacementModifierType<UnlimitedCountPlacement>> UNLIMITED_COUNT = PLACEMENT_MODIFIER.register("unlimited_count", () -> () -> UnlimitedCountPlacement.CODEC);
	public static final RegistryEntry<PlacementModifierType<SnapToLowerNonAirPlacement>> SNAP_TO_LOWER_NON_AIR_PLACEMENT = PLACEMENT_MODIFIER.register("snap_to_lower_non_air_placement", () -> () -> SnapToLowerNonAirPlacement.CODEC);
	public static final RegistryEntry<PlacementModifierType<MinDistanceFromWorldOriginPlacement>> MIN_DISTANCE_FROM_WORLD_ORIGIN_PLACEMENT = PLACEMENT_MODIFIER.register("min_distance_from_world_origin_placement", () -> () -> MinDistanceFromWorldOriginPlacement.CODEC);
}
