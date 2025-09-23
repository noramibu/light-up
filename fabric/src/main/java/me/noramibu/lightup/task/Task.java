package me.noramibu.lightup.task;

import me.noramibu.lightup.model.LightUpType;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

public class Task {
    public final UUID playerUuid;
    public final World world;
    public final BlockState blockState;
    public final int minLightLevel;
    public final boolean includeSkylight;
    public final LightUpType type;
    public final Queue<BlockPos> blocks;
    public final int maxBlocksPerTick;
    public final List<BlockPos> currentPlacementRecord = new ArrayList<>();
    public final int totalBlocks;
    public int placed = 0;
    public boolean cancelled = false;
    public final ServerCommandSource commandSource;
    public final boolean progressEnabled;
    public final String actionBarFormat;

    public Task(UUID playerUuid, World world, BlockState blockState, int minLightLevel, boolean includeSkylight, LightUpType type, Queue<BlockPos> blocks, int maxBlocksPerTick, ServerCommandSource commandSource, boolean progressEnabled, String actionBarFormat) {
        this.playerUuid = playerUuid;
        this.world = world;
        this.blockState = blockState;
        this.minLightLevel = Math.min(15, Math.max(0, minLightLevel));
        this.includeSkylight = includeSkylight;
        this.type = Objects.requireNonNullElse(type, LightUpType.ALL);
        this.blocks = blocks;
        this.maxBlocksPerTick = maxBlocksPerTick;
        this.totalBlocks = blocks.size();
        this.commandSource = commandSource;
        this.progressEnabled = progressEnabled;
        this.actionBarFormat = actionBarFormat;
    }
}


