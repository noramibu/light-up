package me.noramibu.lightup;

import me.noramibu.lightup.command.LightUpCommand;
import me.noramibu.lightup.config.config;
import me.noramibu.lightup.task.TaskManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LightUp implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("light-up");

    private final TaskManager taskManager = new TaskManager();
    private final config cfg = config.loadOrCreate();

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            UUID uuid = handler.player.getUuid();
            taskManager.ensureUndoStack(uuid);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> taskManager.tick(server));

        LightUpCommand.register(taskManager, cfg);

        //: >=1.21.7
        LOGGER.info("Light Up initialized for 1.21.7+");
        //: END

        /*\ >=1.21.0 <1.21.7
        LOGGER.info("Light Up initialized for 1.21.0 - 1.21.6");
        \END */

         /*\ >=1.20.0 <1.21.0
         LOGGER.info("Light Up initialized for 1.20.x");
        \END */

        /*\ <1.20.0
        LOGGER.info("Light Up initialized for 1.19.x");
        \END */


    }
}




