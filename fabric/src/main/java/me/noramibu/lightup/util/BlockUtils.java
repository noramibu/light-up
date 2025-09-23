package me.noramibu.lightup.util;

import me.noramibu.lightup.model.LightUpType;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public final class BlockUtils {
    private BlockUtils() {}

    public static boolean isValidPlacement(World world, BlockPos pos, LightUpType type) {
        if (!world.isAir(pos)) return false;
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        if (belowState.isAir()) return false;
        if (!belowState.isFullCube(world, below)) return false;
        if (belowState.isOf(net.minecraft.block.Blocks.BEDROCK)) return false;
        return switch (type) {
            case ALL -> true;
            case SURFACE -> world.getLightLevel(LightType.SKY, pos) > 0;
            case CAVE -> world.getLightLevel(LightType.SKY, pos) <= 7;
        };
    }
}


