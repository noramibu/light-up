package me.noramibu.lightup.task;

import me.noramibu.lightup.util.BlockUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class TaskManager {
    private final Map<UUID, Task> playerTasks = new HashMap<>();
    private final Map<UUID, Deque<List<BlockPos>>> playerUndo = new HashMap<>();

    public void ensureUndoStack(UUID uuid) {
        playerUndo.computeIfAbsent(uuid, k -> new ArrayDeque<>());
    }

    public boolean hasActiveTask(UUID uuid) {
        return playerTasks.containsKey(uuid);
    }

    public void cancel(UUID uuid) {
        Task t = playerTasks.get(uuid);
        if (t != null) {
            t.cancelled = true;
        }
    }

    public void createTask(ServerPlayer player, Task task) {
        playerTasks.put(player.getUUID(), task);
        task.commandSource.sendSuccess(() -> Component.literal("Light Up started. Scanning " + task.totalBlocks + " blocks..."), false);
    }

    public void tick(MinecraftServer server) {
        List<Task> finishedTasks = new ArrayList<>();
        Iterator<Map.Entry<UUID, Task>> iterator = playerTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next().getValue();
            if (task.cancelled) {
                iterator.remove();
                finishedTasks.add(task);
                continue;
            }
            int processed = 0;
            while (!task.blocks.isEmpty() && processed < task.maxBlocksPerTick) {
                BlockPos pos = task.blocks.poll();
                if (pos == null) {
                    break;
                }
                if (!BlockUtils.isValidPlacement(task.world, pos, task.blockState, task.type)) {
                    continue;
                }
                int lightLevel = task.includeSkylight
                        ? task.world.getMaxLocalRawBrightness(pos)
                        : task.world.getBrightness(LightLayer.BLOCK, pos);
                if (lightLevel >= task.minLightLevel) {
                    continue;
                }
                if (task.world.setBlock(pos, task.blockState, Block.UPDATE_ALL)) {
                    task.placed++;
                    task.currentPlacementRecord.add(pos);
                    if (task.progressEnabled) {
                        int scanned = task.totalBlocks - task.blocks.size();
                        int percentage = Math.round((1F - ((float) task.blocks.size() / (float) task.totalBlocks)) * 100F);
                        String msg = task.actionBarFormat
                                .replace("{ScannedBlocks}", String.valueOf(scanned))
                                .replace("{TotalBlocks}", String.valueOf(task.totalBlocks))
                                .replace("{PlacedLights}", String.valueOf(task.placed))
                                .replace("{CompletedPercentage}", String.valueOf(percentage));
                        ServerPlayer player = server.getPlayerList().getPlayer(task.playerUuid);
                        if (player != null) {
                            player.sendOverlayMessage(Component.literal(msg).withStyle(ChatFormatting.YELLOW));
                        }
                    }
                    break;
                }
                processed++;
            }
            if (task.blocks.isEmpty()) {
                iterator.remove();
                finishedTasks.add(task);
            }
        }

        for (Task task : finishedTasks) {
            finish(task, task.cancelled);
        }
    }

    private void finish(Task task, boolean cancelledFinal) {
        if (!task.currentPlacementRecord.isEmpty()) {
            playerUndo.computeIfAbsent(task.playerUuid, k -> new ArrayDeque<>()).addLast(task.currentPlacementRecord);
        }
        task.commandSource.sendSuccess(
            () -> Component.literal(cancelledFinal ? "Light up task cancelled" : ("Light Up complete. Scanned " + task.totalBlocks + ", Placed " + task.placed + ".")),
            false
        );
    }

    public boolean undo(ServerPlayer player) {
        Deque<List<BlockPos>> stack = playerUndo.computeIfAbsent(player.getUUID(), k -> new ArrayDeque<>());
        if (stack.isEmpty()) {
            return false;
        }
        List<BlockPos> last = stack.removeLast();
        Level world = player.level();
        for (BlockPos pos : last) {
            world.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        return true;
    }
}


