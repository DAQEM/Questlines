package com.daqem.questlines.data;

import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.daqem.questlines.questline.quest.Quest;
import com.daqem.questlines.questline.quest.QuestProgress;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Quest.class, new Quest.Serializer())
            .create();
    public static final Logger LOGGER = LogUtils.getLogger();

    public QuestManager() {
        super(GSON, "questlines/quests");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ActionHolderManager.getInstance().clearActionHolders(QuestlinesActionHolderType.OBJECTIVE);
        List<Quest> tempQuests = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement element = entry.getValue();

            element.getAsJsonObject().addProperty("location", location.toString());

            try {
                Quest quest = GSON.fromJson(element, Quest.class);
                if (quest != null) {
                    tempQuests.add(quest);
                } else {
                    LOGGER.error("Could not deserialize quest {}", location);
                }
            } catch (Exception e) {
                LOGGER.error("Could not deserialize quest {} because: {}", location, e.getMessage());
                throw e;
            }
        }

        LOGGER.info("Loaded {} quests", tempQuests.size());
        Questlines.getInstance().getQuestlineManager().applyQuests(sortQuests(tempQuests));
    }

    public @Nullable Quest getQuest(ResourceLocation location) {
        return Questlines.getInstance().getQuestlineManager().getAllQuests().stream()
                .filter(quest -> quest.getLocation().equals(location))
                .findFirst()
                .orElse(null);
    }

    public @Nullable Objective getObjective(ResourceLocation location) {
        return Questlines.getInstance().getQuestlineManager().getAllQuests().stream()
                .map(Quest::getObjectives)
                .flatMap(List::stream)
                .filter(objective -> objective.getLocation().equals(location))
                .findFirst()
                .orElse(null);
    }

    public static List<Quest> sortQuests(List<Quest> quests) {
        for (Quest quest : quests) {
            if (quest.getParentLocation() != null) {
                Quest parent = quests.stream().filter(q -> q.getLocation().equals(quest.getParentLocation())).findFirst().orElse(null);
                if (parent != null) {
                    quest.setParent(parent);
                    parent.addChild(quest);
                }
            }
        }

        return quests.stream()
                .filter(quest -> quest.getParent() == null)
                .toList();
    }

    public static List<QuestProgress> sortQuestProgresses(List<QuestProgress> quests) {
        for (QuestProgress quest : quests) {
            if (quest.getQuest().getParentLocation() != null) {
                QuestProgress parent = quests.stream().filter(q -> q.getQuest().getLocation().equals(quest.getQuest().getParentLocation())).findFirst().orElse(null);
                if (parent != null) {
                    quest.setParent(parent);
                    parent.addChild(quest);
                }
            }
        }

        return quests.stream()
                .filter(quest -> quest.getParent() == null)
                .toList();
    }
}
