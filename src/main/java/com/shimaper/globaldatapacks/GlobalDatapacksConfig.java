package com.shimaper.globaldatapacks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GlobalDatapacksConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("global-datapacks.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean embedInactiveDatapacks = false;

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try {
            String json = Files.readString(CONFIG_PATH);
            ConfigData data = GSON.fromJson(json, ConfigData.class);
            if (data != null) {
                embedInactiveDatapacks = data.embedInactiveDatapacks;
            }
        } catch (IOException e) {
            GlobalDatapacks.LOGGER.error("[Global Datapacks] Failed to load config", e);
        }
    }

    public static void save() {
        try {
            ConfigData data = new ConfigData();
            data.embedInactiveDatapacks = embedInactiveDatapacks;
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            GlobalDatapacks.LOGGER.error("[Global Datapacks] Failed to save config", e);
        }
    }

    private static class ConfigData {
        boolean embedInactiveDatapacks = false;
    }
}