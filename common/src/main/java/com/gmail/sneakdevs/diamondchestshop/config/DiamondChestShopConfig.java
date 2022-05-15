package com.gmail.sneakdevs.diamondchestshop.config;

import com.gmail.sneakdevs.diamondchestshop.DiamondChestShop;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = DiamondChestShop.MODID)
public class DiamondChestShopConfig implements ConfigData {
    public boolean shopProtection = true;

    public static DiamondChestShopConfig getInstance() {
        return AutoConfig.getConfigHolder(DiamondChestShopConfig.class).getConfig();
    }
}