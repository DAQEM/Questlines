package com.daqem.questlines.fabric;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.QuestlineManager;
import com.daqem.questlines.fabric.data.QuestManagerFabric;
import com.daqem.questlines.fabric.data.QuestlineManagerFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class QuestlinesFabric extends Questlines implements ModInitializer {

    private static final QuestlineManagerFabric QUESTLINE_MANAGER = new QuestlineManagerFabric();
    private static final QuestManagerFabric QUEST_MANAGER = new QuestManagerFabric();

    @Override
    public void onInitialize() {
        init();

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(QUESTLINE_MANAGER);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(QUEST_MANAGER);
    }

    @Override
    public QuestlineManager getQuestlineManager() {
        return QUESTLINE_MANAGER;
    }

    @Override
    public QuestManager getQuestManager() {
        return QUEST_MANAGER;
    }
}
