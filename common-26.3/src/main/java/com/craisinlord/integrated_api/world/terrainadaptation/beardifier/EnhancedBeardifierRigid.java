package com.craisinlord.integrated_api.world.terrainadaptation.beardifier;

import com.craisinlord.integrated_api.world.terrainadaptation.EnhancedTerrainAdaptation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Equivalent to vanilla's Beardifier Rigid, but with an {@link EnhancedTerrainAdaptation} instead of
 * vanilla's Terrain Adjustment.
 */
public record EnhancedBeardifierRigid(BoundingBox pieceBoundingBox, EnhancedTerrainAdaptation pieceTerrainAdaptation, int pieceGroundLevelDelta) {
}