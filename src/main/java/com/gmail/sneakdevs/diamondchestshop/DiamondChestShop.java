package com.gmail.sneakdevs.diamondchestshop;

import net.fabricmc.api.ModInitializer;

public class DiamondChestShop implements ModInitializer {
    public static final String MODID = "diamondchestshop";

    @Override
    public void onInitialize() {
    }

    public static String signTextToReadable(String text) {
        return text.replace("{","").replace("\"", "").replace("text", "").replace("}", "").replace(":", "").replace("$", "").replace(" ", "").toLowerCase();
    }
}
