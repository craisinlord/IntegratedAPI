package com.craisinlord.integrated_api.world.terrainadaptation;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum TerrainAdaptationDirection implements StringRepresentable {
    DOWN("down"),
    UP("up");

    public static final Codec<TerrainAdaptationDirection> CODEC = StringRepresentable.fromEnum(TerrainAdaptationDirection::values);

    private final String name;

    TerrainAdaptationDirection(String name) {
        this.name = name;
    }

    public boolean isInverted() {
        return this == UP;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
