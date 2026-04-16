package me.noramibu.lightup;

import me.noramibu.lightup.command.LightUpCommand;
import me.noramibu.lightup.config.Config;
import me.noramibu.lightup.task.TaskManager;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LightUp.MOD_ID)
public class LightUp {
    public static final String MOD_ID = "lightup";
    private static final Logger LOGGER = LoggerFactory.getLogger("light-up");

    private final TaskManager taskManager = new TaskManager();
    private final Config cfg = Config.loadOrCreate();

    public LightUp() {
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        LOGGER.info("Light Up initialized for NeoForge 26.1.x (built against 26.1.2)");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        LightUpCommand.register(event.getDispatcher(), event.getBuildContext(), taskManager, cfg);
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        taskManager.ensureUndoStack(event.getEntity().getUUID());
    }

    private void onServerTick(ServerTickEvent.Post event) {
        taskManager.tick(event.getServer());
    }
}
