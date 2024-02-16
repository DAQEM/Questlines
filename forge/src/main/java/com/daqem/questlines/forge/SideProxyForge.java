package com.daqem.questlines.forge;

import com.daqem.questlines.Questlines;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class SideProxyForge {

    SideProxyForge() {
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(this::onAddReloadListeners);
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
        }
    }
}
