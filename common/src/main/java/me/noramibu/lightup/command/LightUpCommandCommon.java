package me.noramibu.lightup.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.noramibu.lightup.config.Config;
import me.noramibu.lightup.model.LightUpType;
import me.noramibu.lightup.task.Task;
import me.noramibu.lightup.task.TaskManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Queue;
import java.util.function.Predicate;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class LightUpCommandCommon {
    private LightUpCommandCommon() {
    }

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registryAccess,
            TaskManager manager,
            Config config,
            Predicate<CommandSourceStack> permission
    ) {
        var root = literal("lightup")
                .requires(permission)
                .then(literal("reload").executes(ctx -> {
                    config.reload();
                    ctx.getSource().sendSuccess(() -> Component.literal(config.messageReload), false);
                    return 1;
                }))
                .then(literal("cancel").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }
                    manager.cancel(player.getUUID());
                    return 1;
                }))
                .then(literal("undo").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayer();
                    if (player == null) {
                        return 0;
                    }
                    if (!manager.undo(player)) {
                        player.sendSystemMessage(Component.literal("Nothing to undo."));
                    }
                    return 1;
                }))
                .then(argument("block", BlockStateArgument.block(registryAccess))
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
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, TaskManager manager, Config config) {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }
        if (manager.hasActiveTask(player.getUUID())) {
            player.sendSystemMessage(Component.literal("Please wait for the current Light Up task to complete."));
            return 1;
        }

        BlockInput parsed = BlockStateArgument.getBlock(ctx, "block");
        BlockState state = parsed.getState();
        int min = IntegerArgumentType.getInteger(ctx, "min_light_level");
        int range = IntegerArgumentType.getInteger(ctx, "range");
        boolean includeSky = BoolArgumentType.getBool(ctx, "include_skylight");
        String typeStr = StringArgumentType.getString(ctx, "lightup_type");
        LightUpType type;
        try {
            type = LightUpType.valueOf(typeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            type = LightUpType.ALL;
        }

        Level world = source.getLevel();
        BlockPos origin = player.blockPosition();
        Queue<BlockPos> blocks = collectBlocks(world, origin, range);
        Task task = new Task(
                player.getUUID(),
                world,
                state,
                min,
                includeSky,
                type,
                blocks,
                config.maxBlocksPerTick,
                source,
                config.progressActionBarEnabled,
                config.progressActionBarFormatting
        );
        manager.createTask(player, task);
        return 1;
    }

    private static Queue<BlockPos> collectBlocks(Level world, BlockPos origin, int distanceMax) {
        Queue<BlockPos> blocks = new ArrayDeque<>();
        int minY = Math.max(world.getMinY(), origin.getY() - distanceMax);
        int topAtOrigin = world.getHeight(Heightmap.Types.WORLD_SURFACE, origin.getX(), origin.getZ());
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
