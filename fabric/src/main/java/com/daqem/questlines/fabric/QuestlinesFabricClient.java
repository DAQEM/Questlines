package com.daqem.questlines.fabric;

import com.daqem.questlines.client.QuestlinesClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class QuestlinesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        QuestlinesClient.init();
        registerKeyBindings();
    }

    private void registerKeyBindings() {
        KeyBindingHelper.registerKeyBinding(QuestlinesClient.OPEN_QUEST_SCREEN);
    }
}
