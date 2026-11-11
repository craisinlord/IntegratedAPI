package com.craisinlord.integrated_api.misc.maptrades;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class StructureSpecificMaps {
    public static class TreasureMapForEmeralds extends VillagerTrade {
        private final int emeraldCost;
        private final ResourceKey<Structure> destination;
        private final TagKey<Structure> destinationTag;
        private final String displayName;
        private final Holder.Reference<MapDecorationType> destinationType;
        private final int maxUses;
        private final int villagerXp;
        private final int spawnRegionSearchRadius;

        public TreasureMapForEmeralds(int emeraldCost, String csf, String displayName, Holder.Reference<MapDecorationType> mapIcon, int maxUse, int xp, int spawnRegionSearchRadius) {
            super(new TradeCost(Items.EMERALD, emeraldCost), Optional.of(new TradeCost(Items.COMPASS, 1)), new ItemStackTemplate(Items.FILLED_MAP), maxUse, xp, 0.2F, Optional.empty(), List.of());
            this.emeraldCost = emeraldCost;

            if(csf.startsWith("#")) {
                this.destination = null;
                this.destinationTag = TagKey.create(Registries.STRUCTURE, Identifier.tryParse(csf.replaceFirst("#","")));
            }
            else {
                this.destination = ResourceKey.create(Registries.STRUCTURE, Identifier.tryParse(csf));
                this.destinationTag = null;
            }

            this.displayName = displayName;
            this.destinationType = mapIcon;
            this.maxUses = maxUse;
            this.villagerXp = xp;
            this.spawnRegionSearchRadius = spawnRegionSearchRadius;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(LootContext lootContext) {
            Entity entity = lootContext.getOptionalParameter(LootContextParams.THIS_ENTITY);
            if (entity == null) {
                return null;
            }

            return getOffer(lootContext.getLevel(), entity);
        }

        private HolderSet<Structure> getHolderSet(ServerLevel level) {
            Registry<Structure> registry =
                    level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            return HolderSet.direct(registry.getOrThrow(destination));
        }

        private boolean mapValid(ServerLevel level) {
            HolderSet<Structure> holderSet;
            if (destinationTag == null) {
                holderSet = getHolderSet(level);
            }
            else {
                Optional<HolderSet.Named<Structure>> optionalHolders = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(destinationTag);
                if (optionalHolders.isEmpty()) {
                    return false;
                }
                holderSet = optionalHolders.get();
            }

            boolean isValidSpawning = true;
            for (Holder<Structure> structureHolder : holderSet) {
                if (level.getChunkSource().getGeneratorState().getPlacementsForStructure(structureHolder).isEmpty()) {
                    isValidSpawning = false;
                    break;
                }

                if (level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream()
                        .noneMatch(e -> structureHolder.value().biomes().contains(e)))
                {
                    isValidSpawning = false;
                    break;
                }
            }

            return isValidSpawning;
        }

        private MerchantOffer getOffer(ServerLevel level, Entity entity) {
            if (!mapValid(level)) {
                return null;
            }

            if (destinationTag == null) {
                HolderSet<Structure> holderSet = getHolderSet(level);
                return MerchantMapUpdating.updateMapAsync(
                        entity,
                        emeraldCost,
                        displayName,
                        destinationType,
                        maxUses,
                        villagerXp,
                        holderSet,
                        spawnRegionSearchRadius
                );
            }
            else {
                return MerchantMapUpdating.updateMapAsync(
                        entity,
                        emeraldCost,
                        displayName,
                        destinationType,
                        maxUses,
                        villagerXp,
                        destinationTag,
                        spawnRegionSearchRadius
                );
            }
        }
    }
}
