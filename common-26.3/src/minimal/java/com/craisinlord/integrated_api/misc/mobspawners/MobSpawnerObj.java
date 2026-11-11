package com.craisinlord.integrated_api.misc.mobspawners;

import com.google.gson.annotations.Expose;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public final class MobSpawnerObj {
    @Expose public String name = "";
    @Expose public float weight = 0;
    @Expose public boolean optional = false;
    public transient EntityType<?> entityType;

    public MobSpawnerObj(String name, float weight) {
        this.name = name;
        this.weight = weight;
    }

    public MobSpawnerObj(String name, float weight, boolean optional) {
        this.name = name;
        this.weight = weight;
        this.optional = optional;
    }

    public void setEntityType() {
        Identifier entityId = Identifier.tryParse(name);
        entityType = entityId == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).orElse(null);
        if (entityType == null && !optional) {
            throw new IllegalStateException("Error: " + name + " is not a valid entity ID!");
        }
    }
}
