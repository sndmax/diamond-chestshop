package com.gmail.sneakdevs.diamondchestshop;

import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class ChestshopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("chestshop")
                        .executes(ChestshopCommand::chestshopCommand)
        );
    }

    private static int chestshopCommand(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(new TextComponent("To create a chest shop: \n" + "1) place a chest with a sign attached \n" + "2) write \"buy\" or \"sell\" on the first line \n" + "3) write the quantity of the item to be exchanged on the second line \n" + "4) write the amount of currency to be exchanged on the third line \n" + "5) hold the item to sell in your offhand and click the sign with a " + DiamondEconomyConfig.getCurrencyName(0)), false);
        return 1;
    }
}