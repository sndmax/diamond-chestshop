package com.gmail.sneakdevs.diamondchestshop;

import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class DCSCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal(AutoConfig.getConfigHolder(DEConfig.class).getConfig().commandName)
                        .then(
                                CommandManager.literal("chestshop")
                                        .executes(DCSCommands::chestshopCommand)
                        )
        );
    }

    private static int chestshopCommand(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(new LiteralText("To create a chest shop: \n" + "1) place a chest with a sign attached \n" + "2) write \"buy\" or \"sell\" on the first line \n" + "3) write the quantity of the item to be exchanged on the second line \n" + "4) write the amount of currency to be exchanged on the third line \n" + "5) hold the item to sell in your offhand and click the sign with a " + DEConfig.getCurrencyName()), false);
        return 1;
    }
}