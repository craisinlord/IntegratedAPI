package com.craisinlord.integrated_api.misc.structurepiececounter;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.misc.reload.GsonJsonReloadListener;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StructurePieceCountsManager extends GsonJsonReloadListener {
    public static final StructurePieceCountsManager STRUCTURE_PIECE_COUNTS_MANAGER = new StructurePieceCountsManager();

    private Map<Identifier, List<StructurePieceCountsObj>> structureToPieceCounts = new HashMap<>();
    private final Map<Identifier, Map<Identifier, RequiredPieceNeeds>> cachedRequirePiecesMap = new HashMap<>();
    private final Map<Identifier, Map<Identifier, Integer>> cachedMaxCountPiecesMap = new HashMap<>();

    private StructurePieceCountsManager() {
        super(IntegratedAPI.GSON, "integrated_pieces_spawn_counts");
    }

    private List<StructurePieceCountsObj> getStructurePieceCountsObjs(Identifier fileIdentifier, JsonElement jsonElement) {
        List<StructurePieceCountsObj> piecesSpawnCounts = IntegratedAPI.GSON.fromJson(jsonElement.getAsJsonObject().get("pieces_spawn_counts"), new TypeToken<List<StructurePieceCountsObj>>() {}.getType());
        if (piecesSpawnCounts == null) {
            return List.of();
        }
        for (int i = piecesSpawnCounts.size() - 1; i >= 0; i--) {
            StructurePieceCountsObj entry = piecesSpawnCounts.get(i);
            if (entry.alwaysSpawnThisMany != null && entry.neverSpawnMoreThanThisMany != null && entry.alwaysSpawnThisMany > entry.neverSpawnMoreThanThisMany) {
                throw new IllegalStateException("Invalid piece count entry in " + fileIdentifier);
            }
        }
        return piecesSpawnCounts;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager) {
        Map<Identifier, List<StructurePieceCountsObj>> mapBuilder = new HashMap<>();
        loader.forEach((fileIdentifier, jsonElement) -> {
            try {
                mapBuilder.put(Identifier.parse(jsonElement.getAsJsonObject().get("target_structure").getAsString()), getStructurePieceCountsObjs(fileIdentifier, jsonElement));
            } catch (Exception exception) {
                IntegratedAPI.LOGGER.error("Integrated API Error: Couldn't parse integrated_pieces_spawn_counts file {}", fileIdentifier, exception);
            }
        });
        this.structureToPieceCounts = mapBuilder;
        cachedRequirePiecesMap.clear();
        cachedMaxCountPiecesMap.clear();
    }

    @Override
    protected void onParseError(Identifier fileIdentifier, Exception exception) {
        IntegratedAPI.LOGGER.error("Integrated API Error: Couldn't read piece counts file {}", fileIdentifier, exception);
    }

    public void parseAndAddCountsJSONObj(Identifier structureId, JsonElement jsonElement) {
        this.structureToPieceCounts.computeIfAbsent(structureId, ignored -> new ArrayList<>()).addAll(getStructurePieceCountsObjs(structureId, jsonElement));
    }

    @Nullable
    public Map<Identifier, RequiredPieceNeeds> getRequirePieces(Identifier structureId) {
        if (!this.structureToPieceCounts.containsKey(structureId)) {
            return null;
        }
        return cachedRequirePiecesMap.computeIfAbsent(structureId, ignored -> {
            Map<Identifier, RequiredPieceNeeds> requirePiecesMap = new HashMap<>();
            for (StructurePieceCountsObj entry : this.structureToPieceCounts.getOrDefault(structureId, List.of())) {
                if (entry.alwaysSpawnThisMany != null) {
                    requirePiecesMap.put(Identifier.tryParse(entry.nbtPieceName), new RequiredPieceNeeds(entry.alwaysSpawnThisMany, entry.minimumDistanceFromCenterPiece != null ? entry.minimumDistanceFromCenterPiece : 0));
                }
            }
            return requirePiecesMap;
        });
    }

    public Map<Identifier, Integer> getMaximumCountForPieces(Identifier structureId) {
        return cachedMaxCountPiecesMap.computeIfAbsent(structureId, ignored -> {
            Map<Identifier, Integer> maxCountPiecesMap = new HashMap<>();
            for (StructurePieceCountsObj entry : this.structureToPieceCounts.getOrDefault(structureId, List.of())) {
                if (entry.neverSpawnMoreThanThisMany != null) {
                    maxCountPiecesMap.put(Identifier.tryParse(entry.nbtPieceName), entry.neverSpawnMoreThanThisMany);
                }
            }
            return maxCountPiecesMap;
        });
    }

    public record RequiredPieceNeeds(int maxLimit, int minDistanceFromCenter) {
    }
}
