package me.noramibu.lightup.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.noramibu.lightup.config.Config;
import me.noramibu.lightup.model.LightUpType;
import me.noramibu.lightup.task.Task;
import me.noramibu.lightup.task.TaskManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.BlockState;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.permission.PermissionCheck;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Queue;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class LightUpCommand {
    //: >=1.21.11
    private static final PermissionCheck PERMISSION_CHECK = new PermissionCheck.Require(DefaultPermissions.GAMEMASTERS);
    //: END

    public static void register(TaskManager manager, Config config) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = literal("lightup")

                    /*\ <1.21.11
                .requires(src -> src.hasPermissionLevel(2))
                     \END */

                    //: >1.21.11
                .requires(CommandManager.requirePermissionLevel(PERMISSION_CHECK))
                    //: END

                .then(literal("reload").executes(ctx -> {
                    config.reload();

                    //: >=1.20.0
                   ctx.getSource().sendFeedback(() -> Text.literal(config.messageReload), false);
                    //: END

                   /*\ <1.20.0
                    ctx.getSource().sendFeedback(Text.literal(config.messageReload), false);
                    \END */
                 return 1;
                }))
                .then(literal("cancel").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }
                    manager.cancel(player.getUuid());
                    return 1;
                }))
                .then(literal("undo").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }
                    if (!manager.undo(player)) {
                        player.sendMessage(Text.literal("Nothing to undo."));
                    }
                    return 1;
                }))
                .then(argument("block", BlockStateArgumentType.blockState(registryAccess))
                    .then(argument("min_light_level", IntegerArgumentType.integer(0, 15))
                        .then(argument("range", IntegerArgumentType.integer(0))
                            .then(argument("include_skylight", BoolArgumentType.bool())
                                .then(argument("lightup_type", StringArgumentType.string())
                                    .suggests((ctx, builder) -> {
                                        builder.suggest("surface");
                                        builder.suggest("cave");
                                        builder.suggest("all");
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> execute(ctx, manager, config)))))));

            dispatcher.register(root);
            dispatcher.register(literal("lu").redirect(dispatcher.getRoot().getChild("lightup")));
        });
    }

    private static int execute(com.mojang.brigadier.context.CommandContext<ServerCommandSource> ctx, TaskManager manager, Config config) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Only players can use this command."));
            return 0;
        }
        if (manager.hasActiveTask(player.getUuid())) {
            player.sendMessage(Text.literal("Please wait for the current Light Up task to complete."));
            return 1;
        }
        BlockStateArgument parsed = BlockStateArgumentType.getBlockState(ctx, "block");
        BlockState state = parsed.getBlockState();
        int min = IntegerArgumentType.getInteger(ctx, "min_light_level");
        int range = IntegerArgumentType.getInteger(ctx, "range");
        boolean includeSky = BoolArgumentType.getBool(ctx, "include_skylight");
        String typeStr = StringArgumentType.getString(ctx, "lightup_type");
        LightUpType type;
        try {
            type = LightUpType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            type = LightUpType.ALL;
        }

        World world = source.getWorld();
        BlockPos origin = player.getBlockPos();
        Queue<BlockPos> blocks = collectBlocks(world, origin, range);
        Task task = new Task(player.getUuid(), world, state, min, includeSky, type, blocks, config.maxBlocksPerTick, source, config.progressActionBarEnabled, config.progressActionBarFormatting);
        manager.createTask(player, task);
        return 1;
    }

    private static Queue<BlockPos> collectBlocks(World world, BlockPos origin, int distanceMax) {
        Queue<BlockPos> blocks = new ArrayDeque<>();
        int minY = Math.max(world.getBottomY(), origin.getY() - distanceMax);
        int topAtOrigin = world.getTopY(Heightmap.Type.WORLD_SURFACE, origin.getX(), origin.getZ());
        int maxY = Math.min(topAtOrigin, origin.getY() + distanceMax);
        for (int y = minY; y <= maxY; y++) {
            for (int x = origin.getX() - distanceMax; x <= origin.getX() + distanceMax; x++) {
                for (int z = origin.getZ() - distanceMax; z <= origin.getZ() + distanceMax; z++) {
                    blocks.add(new BlockPos(x, y, z));
                }
            }
        }
        return blocks;
    }
}


