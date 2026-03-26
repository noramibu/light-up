package me.noramibu.lightup.util;

import me.noramibu.lightup.model.LightUpType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockUtils {
    public static boolean isValidPlacement(Level world, BlockPos pos, BlockState blockState, LightUpType type) {
        if (!world.isEmptyBlock(pos)) {
            return false;
        }
        BlockPos below = pos.below();
        BlockState belowState = world.getBlockState(below);
        if (belowState.isAir() || !belowState.isCollisionShapeFullBlock(world, below) || belowState.is(Blocks.BEDROCK)) {
            return false;
        }
        if (!blockState.canSurvive(world, pos)) {
            return false;
        }
        return switch (type) {
            case ALL -> true;
            case SURFACE -> world.getBrightness(LightLayer.SKY, pos) > 0;
            case CAVE -> world.getBrightness(LightLayer.SKY, pos) <= 7;
        };
    }
}


