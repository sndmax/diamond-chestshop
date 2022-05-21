package com.gmail.sneakdevs.diamondchestshopfabric;

import com.gmail.sneakdevs.diamondchestshop.ChestshopCommand;
import com.gmail.sneakdevs.diamondchestshop.DiamondChestShop;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class DiamondChestShopFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ChestshopCommand.register(dispatcher));
        DiamondChestShop.init();
    }
}
