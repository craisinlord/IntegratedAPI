package com.craisinlord.integrated_api.misc.workstations;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.craisinlord.integrated_api.misc.reload.GsonJsonReloadListener;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class WorkstationManager extends GsonJsonReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
    public static final WorkstationManager WORKSTATION_MANAGER = new WorkstationManager();

    private Map<Identifier, List<WorkstationObj>> workstationMap = ImmutableMap.of();

    private WorkstationManager() {
        super(GSON, "integrated_workstations");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager) {
        ImmutableMap.Builder<Identifier, List<WorkstationObj>> builder = ImmutableMap.builder();
        loader.forEach((fileIdentifier, jsonElement) -> {
            try {
                List<WorkstationObj> entries = GSON.fromJson(jsonElement.getAsJsonObject().get("workstations"), new TypeToken<List<WorkstationObj>>() {}.getType());
                if (entries == null) {
                    entries = new ArrayList<>();
                }
                for (int i = entries.size() - 1; i >= 0; i--) {
                    WorkstationObj entry = entries.get(i);
                    entry.setOutputBlock();
                    if (entry.weight <= 0 || entry.outputBlock == null) {
                        entries.remove(i);
                    }
                }
                builder.put(fileIdentifier, List.copyOf(entries));
            } catch (Exception exception) {
                IntegratedAPI.LOGGER.error("Integrated API Error: Couldn't parse workstation list {}", fileIdentifier, exception);
            }
        });
        this.workstationMap = builder.build();
    }

    @Override
    protected void onParseError(Identifier fileIdentifier, Exception exception) {
        IntegratedAPI.LOGGER.error("Integrated API Error: Couldn't read workstation file {}", fileIdentifier, exception);
    }

    public List<WorkstationObj> getWorkstations(Identifier workstationType) {
        return this.workstationMap.getOrDefault(workstationType, List.of());
    }
}
