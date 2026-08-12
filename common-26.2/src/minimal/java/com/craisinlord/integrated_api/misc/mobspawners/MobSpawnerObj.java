package com.craisinlord.integrated_api.misc.mobspawners;

import com.google.gson.annotations.Expose;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public final class MobSpawnerObj {
    @Expose public String name = "";
    @Expose public float weight = 0;
    public transient EntityType<?> entityType;

    public void setEntityType() {
        Identifier entityId = Identifier.tryParse(name);
        entityType = entityId == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).orElse(null);
    }
}
