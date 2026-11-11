package com.craisinlord.integrated_api.mixins.blocks;

import com.craisinlord.integrated_api.IntegratedAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mixin(value = StructureBlockEntity.class, priority = 999)
public abstract class StructureBlockUnlimit {

    @Shadow
    private Identifier structureName;

    @ModifyConstant(method = {"loadAdditional"}, constant = @Constant(intValue = 48))
    public int readNbtUpper(int value) {
        return IntegratedAPI.NEW_STRUCTURE_SIZE;
    }

    @ModifyConstant(method = {"loadAdditional"}, constant = @Constant(intValue = -48))
    public int readNbtLower(int value) {
        return -IntegratedAPI.NEW_STRUCTURE_SIZE;
    }

    /**
     * @author SamB440
     * @reason Optimize searching of structure blocks by searching from the middle out instead of from min corner to max (and reduce streams).
     */
    @Overwrite
    private static Optional<BoundingBox> getEnclosingBoundingBox(Level level, BlockPos selfPos, Identifier structureName) {
        if (level == null) return Optional.empty();
        List<BlockPos> blocks = new ArrayList<>(2);
        final int maxSearch = IntegratedAPI.NEW_STRUCTURE_SIZE + 1;
        for (BlockPos pos : BlockPos.withinBoxByManhattanDistance(selfPos, maxSearch, Math.min(maxSearch, level.getHeight() + 1), maxSearch)) {
            if (level.getBlockEntity(pos) instanceof StructureBlockEntity block) {
                if (block.getMode() == StructureMode.CORNER && Objects.equals(structureName, ((StructureBlockUnlimit) (Object) block).structureName)) {
                    blocks.add(block.getBlockPos());
                }
            }
            if (blocks.size() == 2) {
                break;
            }
        }

        if (blocks.isEmpty()) {
            return Optional.empty();
        }
        BoundingBox result = new BoundingBox(blocks.get(0));
        if (blocks.size() == 1) {
            result.encapsulate(selfPos);
        }
        else {
            result.encapsulate(blocks.get(1));
        }
        return Optional.of(result);
    }
}
