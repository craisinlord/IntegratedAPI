package com.craisinlord.integrated_api.modinit;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.modinit.registry.RegistryEntry;
import com.craisinlord.integrated_api.modinit.registry.ResourcefulRegistries;
import com.craisinlord.integrated_api.modinit.registry.ResourcefulRegistry;
import com.craisinlord.integrated_api.world.structures.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;


public final class IAStructures {
    public static final ResourcefulRegistry<StructureType<?>> STRUCTURE_TYPE = ResourcefulRegistries.create(BuiltInRegistries.STRUCTURE_TYPE, IntegratedAPI.MODID);

    public static RegistryEntry<StructureType<JigsawStructure>> JIGSAW_STRUCTURE = STRUCTURE_TYPE.register("generic_structure", () -> () -> JigsawStructure.CODEC);
    public static RegistryEntry<StructureType<OptionalDependencyStructure>> OPTIONAL_DEPENDENCY_STRUCTURE = STRUCTURE_TYPE.register("optional_dependency_structure", () -> () -> OptionalDependencyStructure.CODEC);
    public static RegistryEntry<StructureType<ModAdaptiveStructure>> MOD_ADAPTIVE_STRUCTURE = STRUCTURE_TYPE.register("mod_adaptive_structure", () -> () -> ModAdaptiveStructure.CODEC);
    public static RegistryEntry<StructureType<NetherJigsawStructure>> NETHER_JIGSAW_STRUCTURE = STRUCTURE_TYPE.register("nether_structure", () -> () -> NetherJigsawStructure.CODEC);
    public static RegistryEntry<StructureType<OverLavaNetherStructure>> OVER_LAVA_NETHER_STRUCTURE = STRUCTURE_TYPE.register("over_lava_nether_structure", () -> () -> OverLavaNetherStructure.CODEC);
    public static RegistryEntry<StructureType<BiomeFacingStructure>> BIOME_FACING_STRUCTURE = STRUCTURE_TYPE.register("biome_facing_structure", () -> () -> BiomeFacingStructure.CODEC);
}


