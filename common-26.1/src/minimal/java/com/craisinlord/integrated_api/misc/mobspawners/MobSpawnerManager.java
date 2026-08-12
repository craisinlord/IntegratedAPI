package com.craisinlord.integrated_api.misc.mobspawners;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.misc.reload.GsonJsonReloadListener;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.Level;

import java.util.List;
import java.util.Map;

public final class MobSpawnerManager extends GsonJsonReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
    public static final MobSpawnerManager MOB_SPAWNER_MANAGER = new MobSpawnerManager();

    private Map<Identifier, List<MobSpawnerObj>> spawnerMap = ImmutableMap.of();

    private MobSpawnerManager() {
        super(GSON, "integrated_structure_spawners");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager) {
        ImmutableMap.Builder<Identifier, List<MobSpawnerObj>> builder = ImmutableMap.builder();
        loader.forEach((fileIdentifier, jsonElement) -> {
            try {
                List<MobSpawnerObj> entries = GSON.fromJson(jsonElement.getAsJsonObject().get("mobs"), new TypeToken<List<MobSpawnerObj>>() {}.getType());
                if (entries == null) {
                    return;
                }
                for (int i = entries.size() - 1; i >= 0; i--) {
                    MobSpawnerObj entry = entries.get(i);
                    entry.setEntityType();
                    if (entry.weight == 0 || entry.entityType == null) {
                        entries.remove(i);
                    } else if (entry.weight < 0) {
                        throw new IllegalStateException("Found negative mob weight for " + entry.name);
                    }
                }
                builder.put(fileIdentifier, List.copyOf(entries));
            } catch (Exception exception) {
                IntegratedAPI.LOGGER.error("Integrated API Error: Couldn't parse spawner mob list {}", fileIdentifier, exception);
            }
        });
        this.spawnerMap = builder.build();
    }

    @Override
    protected void onParseError(Identifier fileIdentifier, Exception exception) {
        IntegratedAPI.LOGGER.error("Integrated API Error: Couldn't read spawner file {}", fileIdentifier, exception);
    }

    public EntityType<?> getSpawnerMob(Identifier spawnerJsonEntry, RandomSource random) {
        List<MobSpawnerObj> spawnerMobEntries = this.spawnerMap.get(spawnerJsonEntry);
        if (spawnerMobEntries == null || spawnerMobEntries.isEmpty()) {
            IntegratedAPI.LOGGER.log(Level.ERROR, "Failed to get mob for {}", spawnerJsonEntry);
            return EntityType.PIG;
        }

        float totalWeight = 0;
        for (MobSpawnerObj entry : spawnerMobEntries) {
            totalWeight += entry.weight;
        }
        if (totalWeight == 0) {
            return null;
        }

        float randomWeight = random.nextFloat() * totalWeight;
        for (MobSpawnerObj entry : spawnerMobEntries) {
            randomWeight -= entry.weight;
            if (randomWeight <= 0) {
                return entry.entityType;
            }
        }
        return spawnerMobEntries.getLast().entityType;
    }
}
