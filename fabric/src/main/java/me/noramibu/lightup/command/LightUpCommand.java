package me.noramibu.lightup.command;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.noramibu.lightup.config.Config;
import me.noramibu.lightup.task.TaskManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class LightUpCommand {
    private static final String COMMAND_PERMISSION = "lightup.command";

    private LightUpCommand() {
    }

    public static void register(TaskManager manager, Config config) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                LightUpCommandCommon.register(
                        dispatcher,
                        registryAccess,
                        manager,
                        config,
                        Permissions.require(COMMAND_PERMISSION, 2)
                ));
    }
}
