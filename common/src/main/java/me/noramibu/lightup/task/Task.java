package me.noramibu.lightup.task;

import me.noramibu.lightup.model.LightUpType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class Task {
    public final UUID playerUuid;
    public final Level world;
    public final BlockState blockState;
    public final int minLightLevel;
    public final boolean includeSkylight;
    public final LightUpType type;
    public final Queue<BlockPos> blocks;
    public final int maxBlocksPerTick;
    public final List<BlockPos> currentPlacementRecord = new ArrayList<>();
    private final Set<Long> positionsLitByPlacedBlocks = new HashSet<>();
    private final int placedLightCoverageRadius;
    public final int totalBlocks;
    public int placed = 0;
    public boolean cancelled = false;
    public final CommandSourceStack commandSource;
    public final boolean progressEnabled;
    public final String actionBarFormat;

    public Task(UUID playerUuid, Level world, BlockState blockState, int minLightLevel, boolean includeSkylight, LightUpType type, Queue<BlockPos> blocks, int maxBlocksPerTick, CommandSourceStack commandSource, boolean progressEnabled, String actionBarFormat) {
        this.playerUuid = playerUuid;
        this.world = world;
        this.blockState = blockState;
        this.minLightLevel = Math.min(15, Math.max(0, minLightLevel));
        this.includeSkylight = includeSkylight;
        this.type = Objects.requireNonNullElse(type, LightUpType.ALL);
        this.blocks = blocks;
        this.maxBlocksPerTick = maxBlocksPerTick;
        int emittedLight = Math.max(0, blockState.getLightEmission());
        this.placedLightCoverageRadius = Math.max(0, emittedLight - this.minLightLevel);
        this.totalBlocks = blocks.size();
        this.commandSource = commandSource;
        this.progressEnabled = progressEnabled;
        this.actionBarFormat = actionBarFormat;
    }

    public boolean isCoveredByPlacedBlockLight(BlockPos pos) {
        return placedLightCoverageRadius > 0 && positionsLitByPlacedBlocks.contains(pos.asLong());
    }

    public void registerPlacedLight(BlockPos center) {
        if (placedLightCoverageRadius <= 0) {
            return;
        }
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        for (int dx = -placedLightCoverageRadius; dx <= placedLightCoverageRadius; dx++) {
            for (int dy = -placedLightCoverageRadius; dy <= placedLightCoverageRadius; dy++) {
                for (int dz = -placedLightCoverageRadius; dz <= placedLightCoverageRadius; dz++) {
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > placedLightCoverageRadius) {
                        continue;
                    }
                    positionsLitByPlacedBlocks.add(BlockPos.asLong(cx + dx, cy + dy, cz + dz));
                }
            }
        }
    }
}


