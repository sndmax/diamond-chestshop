package com.gmail.sneakdevs.diamondchestshop;

import com.gmail.sneakdevs.diamondchestshop.config.DCSConfig;
import com.gmail.sneakdevs.diamondchestshop.interfaces.LockableContainerBlockEntityInterface;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

public class DiamondChestShop implements ModInitializer {
    public static final String MODID = "diamondchestshop";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> DCSCommands.register(dispatcher));
        AutoConfig.register(DCSConfig.class, Toml4jConfigSerializer::new);

    }

    public static String signTextToReadable(String text) {
        return text.replace("{","").replace("\"", "").replace("text", "").replace("}", "").replace(":", "").replace("$", "").replace(" ", "").toLowerCase();
    }

    public static NbtCompound getNbtData(String text) throws CommandSyntaxException {
        NbtCompound nbt = NbtHelper.fromNbtProviderString(text);
        nbt.remove("palette");
        return nbt;
    }
}
