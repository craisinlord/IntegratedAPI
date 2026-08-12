package com.craisinlord.integrated_api.world.structures.placements;

import com.craisinlord.integrated_api.modinit.IAStructurePlacementType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class StrongholdPlacement extends RandomSpreadStructurePlacement {
    public static final MapCodec<StrongholdPlacement> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(StrongholdPlacement::locateOffset),
            FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", FrequencyReductionMethod.DEFAULT).forGetter(StrongholdPlacement::frequencyReductionMethod),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(StrongholdPlacement::frequency),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(StrongholdPlacement::salt),
            ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(StrongholdPlacement::exclusionZone),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("spacing").forGetter(StrongholdPlacement::spacing),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("separation").forGetter(StrongholdPlacement::separation),
            RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(StrongholdPlacement::spreadType),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("chunk_distance_to_first_ring").forGetter(StrongholdPlacement::chunkDistanceToFirstRing),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ring_chunk_thickness").forGetter(StrongholdPlacement::ringChunkThickness),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("max_ring_section").forGetter(StrongholdPlacement::maxRingSection)
    ).apply(instance, instance.stable(StrongholdPlacement::new)));

    private final int chunkDistanceToFirstRing;
    private final int ringChunkThickness;
    private final Optional<Integer> maxRingSection;

    public StrongholdPlacement(Vec3i locateOffset,
                               FrequencyReductionMethod frequencyReductionMethod,
                               Float frequency,
                               Integer salt,
                               Optional<ExclusionZone> exclusionZone,
                               Integer spacing,
                               Integer separation,
                               RandomSpreadType randomSpreadType,
                               Integer chunkDistanceToFirstRing,
                               Integer ringChunkThickness,
                               Optional<Integer> maxRingSection) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, randomSpreadType);
        this.chunkDistanceToFirstRing = chunkDistanceToFirstRing;
        this.ringChunkThickness = ringChunkThickness;
        this.maxRingSection = maxRingSection;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState chunkGeneratorStructureState, int chunkX, int chunkZ) {
        long seed = chunkGeneratorStructureState.getLevelSeed();
        ChunkPos chunkPos = this.getPotentialStructureChunk(seed, chunkX, chunkZ);
        if (chunkPos.x() == chunkX && chunkPos.z() == chunkZ) {
            int chunkDistance = (int) Math.sqrt((chunkX * chunkX) + (chunkZ * chunkZ));

            // Offset the distance so that the first ring is closer to spawn
            int shiftedChunkDistance = chunkDistance + (ringChunkThickness - chunkDistanceToFirstRing);

            // Determine which ring we are in.
            // Non-stronghold rings are even number ringSection.
            // Stronghold rings are odd number ringSection.
            int ringSection = shiftedChunkDistance / ringChunkThickness;

            // Limit number of rings, if max setting is present.
            if (maxRingSection.isPresent()) {
                if (ringSection > maxRingSection.get()) {
                    return false;
                }
            }

            // Only spawn strongholds on odd number sections
            return ringSection % 2 == 1;
        }
        return false;
    }

    @Override
    public StructurePlacementType<?> type() {
        return (StructurePlacementType<?>) IAStructurePlacementType.STRONGHOLD_PLACEMENT;
    }

    public int chunkDistanceToFirstRing() {
        return chunkDistanceToFirstRing;
    }

    public int ringChunkThickness() {
        return ringChunkThickness;
    }

    public Optional<Integer> maxRingSection() {
        return maxRingSection;
    }
}
