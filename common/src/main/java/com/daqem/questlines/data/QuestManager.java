package com.daqem.questlines.data;

import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.config.QuestlinesConfig;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.daqem.questlines.questline.Questline;
import com.daqem.questlines.questline.quest.Quest;
import com.daqem.questlines.questline.quest.QuestProgress;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.google.common.collect.ImmutableMap;
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

import java.util.*;

public class QuestManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Quest.class, new Quest.Serializer())
            .create();
    public static final Logger LOGGER = LogUtils.getLogger();
    private ImmutableMap<ResourceLocation, Quest> quests = ImmutableMap.of();

    public QuestManager() {
        super(GSON, "questlines/quests");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ActionHolderManager.getInstance().clearActionHolders(QuestlinesActionHolderType.OBJECTIVE);
        Map<ResourceLocation, Quest> tempQuests = new HashMap<>();

        if (!QuestlinesConfig.isDebug.get()) {
            object.entrySet().removeIf(entry -> !entry.getKey().getNamespace().equals("debug"));
        }

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement element = entry.getValue();

            element.getAsJsonObject().addProperty("location", location.toString());

            try {
                Quest quest = GSON.fromJson(element, Quest.class);
                if (quest != null) {
                    tempQuests.put(quest.getLocation(), quest);
                } else {
                    LOGGER.error("Could not deserialize quest {}", location);
                }
            } catch (Exception e) {
                LOGGER.error("Could not deserialize quest {} because: {}", location, e.getMessage());
                throw e;
            }
        }

        LOGGER.info("Loaded {} quests", tempQuests.size());
        quests = ImmutableMap.copyOf(tempQuests);
        Questlines.getInstance().getQuestlineManager().applyQuests(sortQuests(tempQuests.values().stream().toList()));
    }

    public Optional<Quest> getQuest(ResourceLocation location) {
        return Optional.ofNullable(quests.get(location));
    }

    public List<Quest> getQuests() {
        return new ArrayList<>(quests.values());
    }

    public Optional<Quest> getStartQuestFor(Questline questline) {
        return getSortedQuests().stream()
                .filter(quest -> quest.getQuestlineLocation().equals(questline.getLocation()))
                .findFirst();
    }

    public List<Quest> getSortedQuests() {
        return sortQuests(new ArrayList<>(quests.values()));
    }

    public Optional<Objective> getObjective(ResourceLocation location) {
        return quests.values().stream()
                .map(Quest::getObjectives)
                .flatMap(List::stream)
                .filter(objective -> objective.getLocation().equals(location))
                .findFirst();
    }

    public void replaceQuests(List<Quest> quests) {
        ImmutableMap.Builder<ResourceLocation, Quest> map = ImmutableMap.builder();
        for (Quest quest : quests) {
            map.put(quest.getLocation(), quest);
        }
        this.quests = map.build();
        LOGGER.info("Updated {} quests", this.quests.size());
    }

    public List<String> getLocationStrings() {
        return quests.keySet().stream().map(ResourceLocation::toString).toList();
    }

    public List<String> getObjectiveLocationStrings() {
        return quests.values().stream()
                .map(Quest::getObjectives)
                .flatMap(List::stream)
                .map(Objective::getLocation)
                .map(ResourceLocation::toString)
                .toList();
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
                .filter(quest -> quest.getParent().isEmpty())
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
                .filter(quest -> quest.getQuestParent().isEmpty())
                .toList();
    }
}
