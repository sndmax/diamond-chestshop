package com.gmail.sneakdevs.diamondchestshopforge;

import com.gmail.sneakdevs.diamondchestshop.DiamondChestShop;
import com.gmail.sneakdevs.diamondchestshopforge.events.RegisterCommandEventHandler_DiamondChestShop;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(DiamondChestShop.MODID)
public class DiamondChestShopForge {
    public DiamondChestShopForge() {
        MinecraftForge.EVENT_BUS.addListener(RegisterCommandEventHandler_DiamondChestShop::diamondchestshop_registerCommandsEvent);
        DiamondChestShop.init();
    }
}
