package com.gmail.sneakdevs.diamondchestshop.config;

import blue.endless.jankson.Comment;
import com.gmail.sneakdevs.diamondchestshop.DiamondChestShop;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = DiamondChestShop.MODID)
public class DiamondChestShopConfig implements ConfigData {
    @Comment("What to protect chest shops from")
    public boolean shopProtectPlayerOpen = true;
    public boolean shopProtectPlayerBreak = true;
    public boolean shopProtectExplosion = true;
    public boolean shopProtectPiston = true;
    public boolean shopProtectHopper = true;
    @Comment("Note: currently doesn't protect double chests")
    public boolean shopProtectHopperMinecart = true;

    @Comment("Name of the command to tell the player how to make a chestshop")
    public String chestshopCommandName = "chestshop";

    @Comment("Whether or not to use the base diamond economy command")
    public boolean useBaseCommand = false;

    public static DiamondChestShopConfig getInstance() {
        return AutoConfig.getConfigHolder(DiamondChestShopConfig.class).getConfig();
    }
}