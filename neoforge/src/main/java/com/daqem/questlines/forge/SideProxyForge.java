package com.daqem.questlines.forge;

import com.daqem.arc.Arc;
import com.daqem.arc.command.argument.ActionArgument;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.client.QuestlinesClient;
import com.daqem.questlines.command.argument.ObjectiveArgument;
import com.daqem.questlines.command.argument.QuestArgument;
import com.daqem.questlines.command.argument.QuestlineArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

public class SideProxyForge {

    SideProxyForge() {
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(this::onAddReloadListeners);

        registerCommandArgumentTypes();
    }

    private void registerCommandArgumentTypes() {
        DeferredRegister<ArgumentTypeInfo<?, ?>> argTypeRegistry = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, Questlines.MOD_ID);
        argTypeRegistry.register("questline", () -> ArgumentTypeInfos.registerByClass(QuestlineArgument.class, SingletonArgumentInfo.contextFree(QuestlineArgument::questline)));
        argTypeRegistry.register("quest", () -> ArgumentTypeInfos.registerByClass(QuestArgument.class, SingletonArgumentInfo.contextFree(QuestArgument::quest)));
        argTypeRegistry.register("objective", () -> ArgumentTypeInfos.registerByClass(ObjectiveArgument.class, SingletonArgumentInfo.contextFree(ObjectiveArgument::objective)));
        argTypeRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(Questlines.getInstance().getQuestlineManager());
        event.addListener(Questlines.getInstance().getQuestManager());
    }

    public static class Server extends SideProxyForge {
        Server() {
        }

    }

    public static class Client extends SideProxyForge {
        Client() {
            QuestlinesClient.init();
            registerEvents();
        }

        private void registerEvents() {
            IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
            eventBus.addListener(this::registerKeyBindings);
        }

        private void registerKeyBindings(RegisterKeyMappingsEvent event) {
            event.register(QuestlinesClient.OPEN_QUEST_SCREEN);
        }
    }
}
