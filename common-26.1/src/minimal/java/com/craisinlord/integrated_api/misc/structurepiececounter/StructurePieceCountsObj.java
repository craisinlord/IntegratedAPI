package com.craisinlord.integrated_api.misc.structurepiececounter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class StructurePieceCountsObj {
    @Expose @SerializedName(value = "nbt_piece_name", alternate = {"nbtPieceName"}) public String nbtPieceName = "";
    @Expose @SerializedName(value = "always_spawn_this_many", alternate = {"alwaysSpawnThisMany"}) public Integer alwaysSpawnThisMany;
    @Expose @SerializedName(value = "never_spawn_more_than_this_many", alternate = {"neverSpawnMoreThanThisMany"}) public Integer neverSpawnMoreThanThisMany;
    @Expose @SerializedName(value = "minimum_distance_from_center_piece", alternate = {"minimumDistanceFromCenterPiece"}) public Integer minimumDistanceFromCenterPiece;
    @Expose public String condition;
}
