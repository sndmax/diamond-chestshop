package com.gmail.sneakdevs.diamondchestshop;

import com.gmail.sneakdevs.diamondchestshop.config.DiamondChestShopConfig;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class DiamondChestShop {
    public static final String MODID = "diamondchestshop";

    public static void init(){
        AutoConfig.register(DiamondChestShopConfig.class, JanksonConfigSerializer::new);
    }

    public static String signTextToReadable(String text) {
        return text.replace("{","").replace("\"", "").replace("text", "").replace("}", "").replace(":", "").replace("$", "").replace(" ", "").toLowerCase();
    }

    public static CompoundTag getNbtData(String text) throws CommandSyntaxException {
        CompoundTag nbt = NbtUtils.snbtToStructure(text);
        nbt.remove("palette");
        return nbt;
    }
}
