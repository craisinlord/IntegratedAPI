package com.craisinlord.integrated_api.misc.reload;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class GsonJsonReloadListener implements ResourceManagerReloadListener {
    private final Gson gson;
    private final FileToIdConverter converter;

    protected GsonJsonReloadListener(Gson gson, String directory) {
        this.gson = gson;
        this.converter = FileToIdConverter.json(directory);
    }

    @Override
    public final void onResourceManagerReload(ResourceManager resourceManager) {
        Map<Identifier, JsonElement> json = new LinkedHashMap<>();
        for (Map.Entry<Identifier, Resource> entry : converter.listMatchingResources(resourceManager).entrySet()) {
            Identifier id = converter.fileToId(entry.getKey());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8))) {
                json.put(id, GsonHelper.fromJson(gson, reader, JsonElement.class));
            } catch (IOException | JsonParseException ex) {
                onParseError(id, ex);
            }
        }
        apply(json, resourceManager);
    }

    protected abstract void apply(Map<Identifier, JsonElement> loader, ResourceManager manager);

    protected abstract void onParseError(Identifier fileIdentifier, Exception exception);
}
