package com.daqem.questlines.fabric;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.command.argument.ObjectiveArgument;
import com.daqem.questlines.command.argument.QuestArgument;
import com.daqem.questlines.command.argument.QuestlineArgument;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;

public class QuestlinesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Questlines.init();
        registerCommandArgumentTypes();
    }

    private void registerCommandArgumentTypes() {
        ArgumentTypeRegistry.registerArgumentType(Questlines.getId("questline"), QuestlineArgument.class, SingletonArgumentInfo.contextFree(QuestlineArgument::questline));
        ArgumentTypeRegistry.registerArgumentType(Questlines.getId("quest"), QuestArgument.class, SingletonArgumentInfo.contextFree(QuestArgument::quest));
        ArgumentTypeRegistry.registerArgumentType(Questlines.getId("objective"), ObjectiveArgument.class, SingletonArgumentInfo.contextFree(ObjectiveArgument::objective));
    }
}
