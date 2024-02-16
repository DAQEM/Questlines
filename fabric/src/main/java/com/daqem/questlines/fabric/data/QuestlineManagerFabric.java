package com.daqem.questlines.fabric.data;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.QuestlineManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class QuestlineManagerFabric extends QuestlineManager implements IdentifiableResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return Questlines.getId("questlines/questlines");
    }
}
