package com.daqem.questlines.neoforge;

import com.daqem.questlines.command.argument.ObjectiveArgument;
import com.daqem.questlines.command.argument.QuestArgument;
import com.daqem.questlines.command.argument.QuestlineArgument;
import com.daqem.questlines.Questlines;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Questlines.MOD_ID)
public class QuestlinesNeoForge {

    public QuestlinesNeoForge(IEventBus eventBus) {
        Questlines.init();
        registerCommandArgumentTypes(eventBus);
    }

    private void registerCommandArgumentTypes(IEventBus eventBus) {
        DeferredRegister<ArgumentTypeInfo<?, ?>> argTypeRegistry = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Questlines.MOD_ID);
        argTypeRegistry.register("questline", () -> ArgumentTypeInfos.registerByClass(QuestlineArgument.class, SingletonArgumentInfo.contextFree(QuestlineArgument::questline)));
        argTypeRegistry.register("quest", () -> ArgumentTypeInfos.registerByClass(QuestArgument.class, SingletonArgumentInfo.contextFree(QuestArgument::quest)));
        argTypeRegistry.register("objective", () -> ArgumentTypeInfos.registerByClass(ObjectiveArgument.class, SingletonArgumentInfo.contextFree(ObjectiveArgument::objective)));
        argTypeRegistry.register(eventBus);
    }
}
