package com.craisinlord.integrated_api.misc.maptrades;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemListing {
    @Nullable
    MerchantOffer getOffer(Entity trader, RandomSource random);
}
