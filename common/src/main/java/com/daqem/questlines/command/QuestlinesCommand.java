package com.daqem.questlines.command;

import com.daqem.questlines.player.QuestlinesServerPlayer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class QuestlinesCommand {

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("questlines")
                .then(Commands.literal("reset")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "target");
                                    if (player instanceof QuestlinesServerPlayer serverPlayer) {
                                        serverPlayer.questlines1_20_1$resetQuestlines();
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}
