package com.craisinlord.integrated_api.world.predicates;

import com.craisinlord.integrated_api.modinit.IAPredicates;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

import java.util.Locale;
import java.util.Map;

public class MatterPhaseRuleTest extends RuleTest {
    public static final MapCodec<MatterPhaseRuleTest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StringRepresentable.fromEnum(MATTER_PHASE::values).fieldOf("phase_to_test_for").forGetter(ruleTest -> ruleTest.phaseToTestFor),
            Codec.BOOL.fieldOf("invert_condition").orElse(false).forGetter(ruleTest -> ruleTest.invertCondition)
    ).apply(instance, MatterPhaseRuleTest::new));

    private final MATTER_PHASE phaseToTestFor;
    private final boolean invertCondition;

    private MatterPhaseRuleTest(MATTER_PHASE phaseToTestFor, boolean invertCondition) {
        this.phaseToTestFor = phaseToTestFor;
        this.invertCondition = invertCondition;
    }

    @Override
    public boolean test(BlockState state, RandomSource random) {
        boolean phaseMatch = false;

        switch (this.phaseToTestFor) {
            case AIR -> phaseMatch = state.isAir();
            case LIQUID -> phaseMatch = !state.getFluidState().isEmpty();
            case SOLID -> phaseMatch = !state.isAir() && state.getFluidState().isEmpty() && state.canOcclude();
            case AIR_RAIL_OR_CHAIN -> phaseMatch = state.isAir() || state.is(Blocks.IRON_CHAIN) || state.is(Blocks.RAIL);
            case LIQUID_RAIL_OR_CHAIN -> phaseMatch = !state.getFluidState().isEmpty() || state.is(Blocks.IRON_CHAIN) || state.is(Blocks.RAIL);
        }

        return this.invertCondition ? !phaseMatch : phaseMatch;
    }

    @Override
    protected RuleTestType<?> getType() {
        return IAPredicates.MATTER_PHASE_RULE_TEST.get();
    }

    public enum MATTER_PHASE implements StringRepresentable {
        SOLID("SOLID"),
        LIQUID("LIQUID"),
        AIR("AIR"),
        AIR_RAIL_OR_CHAIN("AIR_RAIL_OR_CHAIN"),
        LIQUID_RAIL_OR_CHAIN("LIQUID_RAIL_OR_CHAIN");

        private static final Map<String, MATTER_PHASE> BY_NAME = Util.make(Maps.newHashMap(), hashMap -> {
            for (MATTER_PHASE type : values()) {
                hashMap.put(type.name, type);
            }
        });

        private final String name;

        MATTER_PHASE(String name) {
            this.name = name;
        }

        public static MATTER_PHASE byName(String name) {
            return BY_NAME.get(name.toUpperCase(Locale.ROOT));
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
