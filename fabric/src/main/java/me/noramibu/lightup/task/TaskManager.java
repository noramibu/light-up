package me.noramibu.lightup.task;

import me.noramibu.lightup.model.LightUpType;
import me.noramibu.lightup.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

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
        if (t != null) t.cancelled = true;
    }

    public void createTask(ServerPlayerEntity player, World world, Task task) {
        playerTasks.put(player.getUuid(), task);
        //: >=1.20.0
       task.commandSource.sendFeedback(() -> Text.literal("Light Up started. Scanning " + task.totalBlocks + " blocks..."), false);
        //: END      /*\ <1.20.0

        task.commandSource.sendFeedback(Text.literal("Light Up started. Scanning " + task.totalBlocks + " blocks..."), false);
        \END */
 }

    public void tick(MinecraftServer server) {
        for (Task task : new ArrayList<>(playerTasks.values())) {
            if (task.cancelled) {
                finish(server, task, true);
                continue;
            }
            int processed = 0;
            while (!task.blocks.isEmpty() && processed < task.maxBlocksPerTick) {
                BlockPos pos = task.blocks.poll();
                if (pos == null) break;
                if (!BlockUtils.isValidPlacement(task.world, pos, task.type)) continue;
                int lightLevel = task.includeSkylight
                        ? task.world.getLightLevel(pos)
                        : task.world.getLightLevel(LightType.BLOCK, pos);
                if (lightLevel >= task.minLightLevel) continue;
                if (task.world.setBlockState(pos, task.blockState, Block.NOTIFY_ALL)) {
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
                        ServerPlayerEntity player = server.getPlayerManager().getPlayer(task.playerUuid);
                        if (player != null) {
                            // Use overlay message as action bar equivalent
                            player.sendMessage(Text.literal(msg).formatted(Formatting.YELLOW), true);
                        }
                    }
                    processed++;
                    break;
                }
                processed++;
            }
            if (task.blocks.isEmpty()) {
                finish(server, task, false);
            }
        }
    }

    private void finish(MinecraftServer server, Task task, boolean cancelled) {
        playerTasks.remove(task.playerUuid);
        if (!task.currentPlacementRecord.isEmpty()) {
            playerUndo.computeIfAbsent(task.playerUuid, k -> new ArrayDeque<>()).addLast(task.currentPlacementRecord);
        }
        boolean cancelledFinal = cancelled;
        //: >=1.20.0
       task.commandSource.sendFeedback(() -> Text.literal(cancelledFinal ? "Light up task cancelled" : ("Light Up complete. Scanned " + task.totalBlocks + ", Placed " + task.placed + ".")), false);
        //: END      /*\ <1.20.0

        task.commandSource.sendFeedback(Text.literal(cancelledFinal ? "Light up task cancelled" : ("Light Up complete. Scanned " + task.totalBlocks + ", Placed " + task.placed + ".")), false);
        \END */
 }

    public boolean undo(ServerPlayerEntity player) {
        Deque<List<BlockPos>> stack = playerUndo.computeIfAbsent(player.getUuid(), k -> new ArrayDeque<>());
        if (stack.isEmpty()) return false;
        List<BlockPos> last = stack.removeLast();
        //: >=1.21.7
     var world = player.getCommandSource().getWorld();
        //: END      /*\ <=1.21.6

        var world = player.getWorld();
        \END */
     for (BlockPos pos : last) {
            world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        }
        return true;
    }
}


