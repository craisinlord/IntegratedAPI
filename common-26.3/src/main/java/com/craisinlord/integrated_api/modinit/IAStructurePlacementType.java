package com.craisinlord.integrated_api.modinit;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.registry.RegistryEntry;
import com.craisinlord.integrated_api.modinit.registry.ResourcefulRegistries;
import com.craisinlord.integrated_api.modinit.registry.ResourcefulRegistry;
import com.craisinlord.integrated_api.world.structures.placements.StrongholdPlacement;
import com.craisinlord.integrated_api.world.structures.placements.AdvancedRandomSpread;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;


public final class IAStructurePlacementType {
    public static final ResourcefulRegistry<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPE = ResourcefulRegistries.create(BuiltInRegistries.STRUCTURE_PLACEMENT, IntegratedAPI.MODID);

    public static final RegistryEntry<StructurePlacementType<AdvancedRandomSpread>> ADVANCED_RANDOM_SPREAD = STRUCTURE_PLACEMENT_TYPE.register("advanced_random_spread", () -> () -> AdvancedRandomSpread.CODEC);
    public static final RegistryEntry<StructurePlacementType<StrongholdPlacement>> STRONGHOLD_PLACEMENT = STRUCTURE_PLACEMENT_TYPE.register("stronghold", () -> () -> StrongholdPlacement.CODEC);
}
