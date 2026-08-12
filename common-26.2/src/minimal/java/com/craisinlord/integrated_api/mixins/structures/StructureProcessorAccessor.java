package com.craisinlord.integrated_api.mixins.structures;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import com.mojang.serialization.MapCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureProcessor.class)
public interface StructureProcessorAccessor {
    @Invoker("codec")
    MapCodec<? extends StructureProcessor> callCodec();
}

