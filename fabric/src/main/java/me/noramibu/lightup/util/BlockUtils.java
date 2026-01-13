package me.noramibu.lightup.util;

import me.noramibu.lightup.model.LightUpType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public final class BlockUtils {
    public static boolean isValidPlacement(World world, BlockPos pos, LightUpType type) {
        if (!world.isAir(pos)) {
            return false;
        }
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        if (belowState.isAir() || belowState.isFullCube(world, below) || belowState.isOf(Blocks.BEDROCK)) {
            return false;
        }
        return switch (type) {
            case ALL -> true;
            case SURFACE -> world.getLightLevel(LightType.SKY, pos) > 0;
            case CAVE -> world.getLightLevel(LightType.SKY, pos) <= 7;
        };
    }
}


