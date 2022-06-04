package com.gmail.sneakdevs.diamondchestshop.config;

import blue.endless.jankson.Comment;
import com.gmail.sneakdevs.diamondchestshop.DiamondChestShop;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.server.level.ServerPlayer;

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

    @Comment("Whether or not to use the base diamond economy command")
    public boolean useBaseCommand = true;

    @Comment("Name of the command to tell the player how to make a chestshop (null to disable)")
    public String chestshopCommandName = "chestshop";

    @Comment("Max number of shops a player can have at once")
    public int playerMaxShops = 500;

    public static DiamondChestShopConfig getInstance() {
        return AutoConfig.getConfigHolder(DiamondChestShopConfig.class).getConfig();
    }

    public static int getPlayerMaxShops(ServerPlayer player){
        if (Permissions.check(player, DiamondChestShop.MODID + ".infiniteshops")) {
            return -1;
        }
        if (Permissions.check(player, DiamondChestShop.MODID + ".noshops")) {
            return 0;
        }
        int shops = getInstance().playerMaxShops;
        if (Permissions.check(player, DiamondChestShop.MODID + ".quintupleshopcount")) {
            return shops * 5;
        }
        if (Permissions.check(player, DiamondChestShop.MODID + ".quadrupleshopcount")) {
            return shops * 4;
        }
        if (Permissions.check(player, DiamondChestShop.MODID + ".tripleshopcount")) {
            return shops * 3;
        }
        if (Permissions.check(player, DiamondChestShop.MODID + ".doubleshopcount")) {
            return shops * 2;
        }
        if (Permissions.check(player, DiamondChestShop.MODID + ".halfshopcount")) {
            return shops / 2;
        }
        return shops;
    }
}