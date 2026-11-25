package com.craisinlord.integrated_api.mixins.features;

import com.craisinlord.integrated_api.modinit.IATags;
import com.craisinlord.integrated_api.utils.StructurePieceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps trees from growing into or right next to structures in the tree_free_structures tag.
 */
@Mixin(TreeFeature.class)
public class NoTreesInStructuresMixin {

    private static final int INTEGRATED_API_TREE_DISTANCE = 4;

    @Inject(
            method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void integrated_api_noTrees(FeaturePlaceContext<TreeConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        // Only natural generation. Saplings grown by players are never blocked.
        if (!(context.level() instanceof WorldGenRegion worldGenRegion)) {
            return;
        }

        Registry<Structure> structureRegistry = worldGenRegion.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        if (StructurePieceUtils.isNearPiece(worldGenRegion, context.origin(), INTEGRATED_API_TREE_DISTANCE,
                structure -> structureRegistry.wrapAsHolder(structure).is(IATags.TREE_FREE_STRUCTURES))) {
            cir.setReturnValue(false);
        }
    }
}
