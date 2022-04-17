package com.gmail.sneakdevs.diamondchestshop;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class DiamondChestShop implements ModInitializer {
    public static final String MODID = "diamondchestshop";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> DCSCommands.register(dispatcher));
    }

    public static String signTextToReadable(String text) {
        return text.replace("{","").replace("\"", "").replace("text", "").replace("}", "").replace(":", "").replace("$", "").replace(" ", "").toLowerCase();
    }
}
