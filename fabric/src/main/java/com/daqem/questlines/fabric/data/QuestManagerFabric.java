package com.daqem.questlines.fabric.data;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.QuestManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class QuestManagerFabric extends QuestManager implements IdentifiableResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return Questlines.getId("questlines/quests");
    }
}
