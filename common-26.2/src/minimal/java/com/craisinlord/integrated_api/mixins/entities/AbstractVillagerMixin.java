package com.craisinlord.integrated_api.mixins.entities;

import com.craisinlord.integrated_api.misc.trades.MerchantTrades;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractVillager.class)
public class AbstractVillagerMixin {

    @ModifyExpressionValue(method = "addOffersFromTradeSet",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/TradeSet;getTrades()Lnet/minecraft/core/HolderSet;"))
    private HolderSet<VillagerTrade> integratedapi_addRegisteredTrades(HolderSet<VillagerTrade> trades, ServerLevel level, MerchantOffers offers, ResourceKey<TradeSet> resourceKey) {
        return MerchantTrades.withAdditionalTrades((AbstractVillager) (Object) this, resourceKey, trades);
    }
}
