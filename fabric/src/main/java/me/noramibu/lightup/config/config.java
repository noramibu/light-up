package me.noramibu.lightup.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class config {
    // Messages
    public String messageReload = translate("&aLight Up has been reloaded!");
    public String messageOnlyPlayerCommand = translate("&cThis command can only be executed by a player");
    public String messageNoActiveTask = translate("&eThere is no active light up tasks");
    public String messageNothingToUndo = translate("&cThere is nothing to undo");
    public String messageWaitComplete = translate("&ePlease wait while your current light up task to complete or cancelled!");
    public String messageLightUpBegin = translate("&7Please wait while we light up your surroundings!");
    public String messageLightUpCompleteTemplate = translate("&aSuccessfully lid up &b{TotalBlocks} &ablocks with &e{TotalPlaced} &alight sources placed!");
    public String messageLightUpCancelled = translate("&cLight up task cancelled");
    public String messageUndoUnloadedWorld = translate("&cUndo cancelled! Destination world is not loaded!");
    public String messageUndoCompleteTemplate = translate("&dLast light up task had been undone, reverted {BlocksUndone} light sources!");

    // Options
    public int maxBlocksPerTick = 4000;
    public boolean progressActionBarEnabled = true;
    public String progressActionBarFormatting = translate("&eLight Up Task: &a{ScannedBlocks}&b/{TotalBlocks} {CompletedPercentage}% &e({PlacedLights} lights placed)");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", "light-up.json");

    public static config loadOrCreate() {
        config cfg = new config();
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                write(cfg);
                return cfg;
            }
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                if (root == null) return cfg;
                JsonObject messages = root.has("messages") && root.get("messages").isJsonObject() ? root.getAsJsonObject("messages") : new JsonObject();
                JsonObject options = root.has("options") && root.get("options").isJsonObject() ? root.getAsJsonObject("options") : new JsonObject();

                cfg.messageReload = getOr(messages, "reload", cfg.messageReload);
                cfg.messageOnlyPlayerCommand = getOr(messages, "onlyPlayerCommand", cfg.messageOnlyPlayerCommand);
                cfg.messageNoActiveTask = getOr(messages, "noActiveTask", cfg.messageNoActiveTask);
                cfg.messageNothingToUndo = getOr(messages, "nothingToUndo", cfg.messageNothingToUndo);
                cfg.messageWaitComplete = getOr(messages, "waitComplete", cfg.messageWaitComplete);
                cfg.messageLightUpBegin = getOr(messages, "lightUpBegin", cfg.messageLightUpBegin);
                cfg.messageLightUpCompleteTemplate = getOr(messages, "lightUpComplete", cfg.messageLightUpCompleteTemplate);
                cfg.messageLightUpCancelled = getOr(messages, "lightUpCancelled", cfg.messageLightUpCancelled);
                cfg.messageUndoUnloadedWorld = getOr(messages, "undoUnloadedWorld", cfg.messageUndoUnloadedWorld);
                cfg.messageUndoCompleteTemplate = getOr(messages, "undoComplete", cfg.messageUndoCompleteTemplate);

                cfg.maxBlocksPerTick = options.has("maxBlocksPerTick") ? options.get("maxBlocksPerTick").getAsInt() : cfg.maxBlocksPerTick;
                cfg.progressActionBarEnabled = options.has("progressActionBarEnabled") && options.get("progressActionBarEnabled").getAsBoolean();
                cfg.progressActionBarFormatting = getOr(options, "progressActionBarFormatting", cfg.progressActionBarFormatting);
            }
        } catch (IOException ignored) {}
        // Always rewrite to ensure new fields are present
        try { write(cfg); } catch (IOException ignored) {}
        return cfg;
    }

    public void reload() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                return;
            }
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                if (root == null) return;
                JsonObject messages = root.has("messages") && root.get("messages").isJsonObject() ? root.getAsJsonObject("messages") : new JsonObject();
                JsonObject options = root.has("options") && root.get("options").isJsonObject() ? root.getAsJsonObject("options") : new JsonObject();

                this.messageReload = getOr(messages, "reload", this.messageReload);
                this.messageOnlyPlayerCommand = getOr(messages, "onlyPlayerCommand", this.messageOnlyPlayerCommand);
                this.messageNoActiveTask = getOr(messages, "noActiveTask", this.messageNoActiveTask);
                this.messageNothingToUndo = getOr(messages, "nothingToUndo", this.messageNothingToUndo);
                this.messageWaitComplete = getOr(messages, "waitComplete", this.messageWaitComplete);
                this.messageLightUpBegin = getOr(messages, "lightUpBegin", this.messageLightUpBegin);
                this.messageLightUpCompleteTemplate = getOr(messages, "lightUpComplete", this.messageLightUpCompleteTemplate);
                this.messageLightUpCancelled = getOr(messages, "lightUpCancelled", this.messageLightUpCancelled);
                this.messageUndoUnloadedWorld = getOr(messages, "undoUnloadedWorld", this.messageUndoUnloadedWorld);
                this.messageUndoCompleteTemplate = getOr(messages, "undoComplete", this.messageUndoCompleteTemplate);

                this.maxBlocksPerTick = options.has("maxBlocksPerTick") ? options.get("maxBlocksPerTick").getAsInt() : this.maxBlocksPerTick;
                this.progressActionBarEnabled = options.has("progressActionBarEnabled") && options.get("progressActionBarEnabled").getAsBoolean();
                this.progressActionBarFormatting = getOr(options, "progressActionBarFormatting", this.progressActionBarFormatting);
            }
        } catch (IOException ignored) {}
    }

    private static void write(config cfg) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject messages = new JsonObject();
        messages.addProperty("reload", cfg.messageReload);
        messages.addProperty("onlyPlayerCommand", cfg.messageOnlyPlayerCommand);
        messages.addProperty("noActiveTask", cfg.messageNoActiveTask);
        messages.addProperty("nothingToUndo", cfg.messageNothingToUndo);
        messages.addProperty("waitComplete", cfg.messageWaitComplete);
        messages.addProperty("lightUpBegin", cfg.messageLightUpBegin);
        messages.addProperty("lightUpComplete", cfg.messageLightUpCompleteTemplate);
        messages.addProperty("lightUpCancelled", cfg.messageLightUpCancelled);
        messages.addProperty("undoUnloadedWorld", cfg.messageUndoUnloadedWorld);
        messages.addProperty("undoComplete", cfg.messageUndoCompleteTemplate);

        JsonObject options = new JsonObject();
        options.addProperty("maxBlocksPerTick", cfg.maxBlocksPerTick);
        options.addProperty("progressActionBarEnabled", cfg.progressActionBarEnabled);
        options.addProperty("progressActionBarFormatting", cfg.progressActionBarFormatting);

        root.add("messages", messages);
        root.add("options", options);

        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
    }

    public String formatComplete(int totalBlocks, int totalPlaced) {
        return messageLightUpCompleteTemplate
            .replace("{TotalBlocks}", String.valueOf(totalBlocks))
            .replace("{TotalPlaced}", String.valueOf(totalPlaced));
    }

    public String formatUndoComplete(int blocksUndone) {
        return messageUndoCompleteTemplate.replace("{BlocksUndone}", String.valueOf(blocksUndone));
    }

    private static String translate(String s) {
        return s.replace('&', '\u00A7');
    }

    private static String getOr(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }
}


