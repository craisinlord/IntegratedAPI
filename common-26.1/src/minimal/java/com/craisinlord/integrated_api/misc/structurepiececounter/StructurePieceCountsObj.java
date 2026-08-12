package com.craisinlord.integrated_api.misc.structurepiececounter;

import com.google.gson.annotations.Expose;

public final class StructurePieceCountsObj {
    @Expose public String nbtPieceName = "";
    @Expose public Integer alwaysSpawnThisMany;
    @Expose public Integer neverSpawnMoreThanThisMany;
    @Expose public Integer minimumDistanceFromCenterPiece;
    @Expose public String condition;
}
