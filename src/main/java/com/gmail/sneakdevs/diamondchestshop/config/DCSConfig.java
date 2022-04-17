package com.gmail.sneakdevs.diamondchestshop.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "diamond_chest_shop")
public class DCSConfig implements ConfigData {
    public boolean shopProtection = true;

    @SuppressWarnings("unused")
    public static DCSConfig getInstance() {
        return AutoConfig.getConfigHolder(DCSConfig.class).getConfig();
    }
}