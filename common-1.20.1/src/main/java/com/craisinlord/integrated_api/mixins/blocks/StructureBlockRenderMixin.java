package com.craisinlord.integrated_api.mixins.blocks;

import com.craisinlord.integrated_api.IntegratedAPI;
import net.minecraft.client.renderer.blockentity.StructureBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = StructureBlockRenderer.class, priority = 900)
public class StructureBlockRenderMixin {

    /**
     * @reason Increase the distance that the bounding box can be seen up to 256 blocks
     * @author SamB440/Cotander
     */
    @ModifyConstant(method = "getViewDistance", constant = @Constant(intValue = 96), require = 0)
    public int getRenderDistance(int value) {
        return IntegratedAPI.NEW_STRUCTURE_SIZE / 2;
    }
}