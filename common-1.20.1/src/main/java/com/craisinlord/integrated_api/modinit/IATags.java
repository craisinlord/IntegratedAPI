package com.craisinlord.integrated_api.modinit;

import com.craisinlord.integrated_api.IntegratedAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class IATags {
    public static void initTags() {}

    public static TagKey<Structure> LARGER_LOCATE_SEARCH = TagKey.create(Registries.STRUCTURE,
            new ResourceLocation(IntegratedAPI.MODID, "larger_locate_search"));

    public static TagKey<Structure> UNSKIPPABLE_STRUCTURES = TagKey.create(Registries.STRUCTURE,
            new ResourceLocation(IntegratedAPI.MODID, "unskippable_structures"));

    public static TagKey<Structure> DISABLED_STRUCTURES = TagKey.create(Registries.STRUCTURE,
            new ResourceLocation(IntegratedAPI.MODID, "disabled_structures"));

    public static TagKey<Feature<?>> SKIPPABLE_FEATURES = TagKey.create(Registries.FEATURE,
            new ResourceLocation(IntegratedAPI.MODID, "skippable_features"));
}
