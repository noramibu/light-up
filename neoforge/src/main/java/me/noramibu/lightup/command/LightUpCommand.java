package me.noramibu.lightup.command;

import com.mojang.brigadier.CommandDispatcher;
import me.noramibu.lightup.config.Config;
import me.noramibu.lightup.task.TaskManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class LightUpCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, TaskManager manager, Config config) {
        LightUpCommandCommon.register(
                dispatcher,
                registryAccess,
                manager,
                config,
                Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)
        );
    }
}


