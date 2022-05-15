package com.gmail.sneakdevs.diamondchestshopforge.events;

import com.gmail.sneakdevs.diamondchestshop.ChestshopCommand;
import net.minecraftforge.event.RegisterCommandsEvent;

public class RegisterCommandEventHandler_DiamondChestShop {
    public static void diamondchestshop_registerCommandsEvent(RegisterCommandsEvent event) {
        ChestshopCommand.register(event.getDispatcher());
    }
}
