package com.craisinlord.integrated_api.mixins.features;

import com.craisinlord.integrated_api.modinit.IATags;
import com.craisinlord.integrated_api.utils.GeneralUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


@Mixin(GeodeFeature.class)
public class NoGeodesInStructuresMixin {

    @Inject(
            method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void integrated_api_noGeodes(FeaturePlaceContext<BlockStateConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if (!(context.level() instanceof WorldGenRegion worldGenRegion)) {
            return;
        }

        Registry<Structure> structureRegistry = worldGenRegion.registryAccess().registry(Registries.STRUCTURE).get();

        List<StructureStart> structureStarts = GeneralUtils.inboundsValidStartsForAllStructure(
                worldGenRegion,
                context.origin(),
                struct -> structureRegistry.getHolderOrThrow(structureRegistry.getResourceKey(struct).get()).is(IATags.LARGER_LOCATE_SEARCH));

        if (!structureStarts.isEmpty()) {
            cir.setReturnValue(false);
        }
    }
}