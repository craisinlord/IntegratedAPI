package com.craisinlord.integrated_api.world.terrainadaptation.beardifier;

import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptation;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;

/**
 * Container for JigsawJunction + additional info used when processing {@link EnhancedTerrainAdaptation}s.
 */
public record EnhancedJigsawJunction(JigsawJunction jigsawJunction, EnhancedTerrainAdaptation pieceTerrainAdaptation) {
}