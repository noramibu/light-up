package me.noramibu.lightup.command;

import me.noramibu.lightup.config.Config;
import me.noramibu.lightup.task.TaskManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;

public final class LightUpCommand {
    private LightUpCommand() {
    }

    public static void register(TaskManager manager, Config config) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                LightUpCommandCommon.register(
                        dispatcher,
                        registryAccess,
                        manager,
                        config,
                        Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)
                ));
    }
}
