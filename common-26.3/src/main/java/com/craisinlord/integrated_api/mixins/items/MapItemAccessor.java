package com.craisinlord.integrated_api.mixins.items;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// Source: https://github.com/thebrightspark/AsyncLocator/blob/1.19.x/src/main/java/brightspark/asynclocator/mixins/MapItemAccess.java
@Mixin(MapItem.class)
public interface MapItemAccessor {
    @Invoker("createNewSavedData")
    static MapId callCreateNewSavedData(Level level, int x, int z, int scale, boolean trackingPosition, boolean unlimitedTracking, ResourceKey<Level> dimension) {
        throw new UnsupportedOperationException();
    }
}
