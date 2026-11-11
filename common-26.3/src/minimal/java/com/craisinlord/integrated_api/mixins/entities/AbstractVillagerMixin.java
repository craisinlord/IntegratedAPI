package com.craisinlord.integrated_api.mixins.entities;

import com.craisinlord.integrated_api.misc.maptrades.MerchantTrades;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {

    @WrapOperation(
            method = "addOffersFromTradeSet",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/AbstractVillager;addOffersFromItemListings(Lnet/minecraft/world/level/storage/loot/LootContext;Lnet/minecraft/world/item/trading/MerchantOffers;Lnet/minecraft/core/HolderSet;I)V")
    )
    private void integratedapi_addStructureMapTrades(LootContext lootContext, MerchantOffers offers, HolderSet<VillagerTrade> potentialOffers, int numberOfOffers, Operation<Void> original, @Local(argsOnly = true) ResourceKey<TradeSet> tradeSet) {
        MerchantTrades.addOffers((AbstractVillager) (Object) this, tradeSet, lootContext, offers, potentialOffers, numberOfOffers, true,
                () -> original.call(lootContext, offers, potentialOffers, numberOfOffers));
    }

    @WrapOperation(
            method = "addOffersFromTradeSet",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/AbstractVillager;addOffersFromItemListingsWithoutDuplicates(Lnet/minecraft/world/level/storage/loot/LootContext;Lnet/minecraft/world/item/trading/MerchantOffers;Lnet/minecraft/core/HolderSet;I)V")
    )
    private void integratedapi_addStructureMapTradesWithoutDuplicates(LootContext lootContext, MerchantOffers offers, HolderSet<VillagerTrade> potentialOffers, int numberOfOffers, Operation<Void> original, @Local(argsOnly = true) ResourceKey<TradeSet> tradeSet) {
        MerchantTrades.addOffers((AbstractVillager) (Object) this, tradeSet, lootContext, offers, potentialOffers, numberOfOffers, false,
                () -> original.call(lootContext, offers, potentialOffers, numberOfOffers));
    }
}
