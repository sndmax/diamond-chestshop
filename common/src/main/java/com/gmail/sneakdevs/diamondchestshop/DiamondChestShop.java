package com.gmail.sneakdevs.diamondchestshop;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class DiamondChestShop {
    public static final String MODID = "diamondchestshop";

    public static String signTextToReadable(String text) {
        return text.replace("{","").replace("\"", "").replace("text", "").replace("}", "").replace(":", "").replace("$", "").replace(" ", "").toLowerCase();
    }

    public static CompoundTag getNbtData(String text) throws CommandSyntaxException {
        CompoundTag nbt = NbtUtils.snbtToStructure(text);
        nbt.remove("palette");
        return nbt;
    }
}
