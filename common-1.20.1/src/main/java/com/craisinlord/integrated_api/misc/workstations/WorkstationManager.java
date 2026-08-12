package com.craisinlord.integrated_api.misc.workstations;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkstationManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().setLenient().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
    public static final WorkstationManager WORKSTATION_MANAGER = new WorkstationManager();

    private Map<ResourceLocation, List<WorkstationObj>> workstationMap = ImmutableMap.of();

    public WorkstationManager() {
        super(GSON, "integrated_workstations");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> loader, ResourceManager manager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, List<WorkstationObj>> builder = ImmutableMap.builder();
        loader.forEach((fileIdentifier, jsonElement) -> {
            try {
                List<WorkstationObj> workstationEntries = GSON.fromJson(jsonElement.getAsJsonObject().get("workstations"), new TypeToken<List<WorkstationObj>>() {}.getType());
                if (workstationEntries == null) {
                    workstationEntries = new ArrayList<>();
                }

                for (int i = workstationEntries.size() - 1; i >= 0; i--) {
                    WorkstationObj entry = workstationEntries.get(i);
                    entry.setOutputBlock();
                    if (entry.weight <= 0) {
                        workstationEntries.remove(i);
                    }
                }

                builder.put(fileIdentifier, List.copyOf(workstationEntries));
            } catch (Exception exception) {
                IntegratedAPI.LOGGER.error("Integrated API Error: Couldn't parse workstation list {}", fileIdentifier, exception);
            }
        });

        this.workstationMap = builder.build();
    }

    public List<WorkstationObj> getWorkstations(ResourceLocation workstationType) {
        return this.workstationMap.getOrDefault(workstationType, List.of());
    }
}
