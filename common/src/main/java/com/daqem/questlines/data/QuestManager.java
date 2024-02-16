package com.daqem.questlines.data;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.questline.quest.Quest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class QuestManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Quest.class, new Quest.Serializer())
            .create();

    public QuestManager() {
        super(GSON, "questlines/quests");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Questlines.LOGGER.info("Loaded quests: " + object.size());
    }
}
