package com.shimaper.globaldatapacks;

import net.fabricmc.api.ClientModInitializer;

public class GlobalDatapacksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GlobalDatapacks.LOGGER.info("Global Datapacks client initialized");
    }
}