package com.daqem.questlines.data;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.config.QuestlinesConfig;
import com.daqem.questlines.questline.Questline;
import com.daqem.questlines.questline.quest.Quest;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.*;

public class QuestlineManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Questline.class, new Questline.Serializer())
            .create();

    private static final Logger LOGGER = LogUtils.getLogger();
    protected ImmutableMap<ResourceLocation, Questline> questlines = ImmutableMap.of();

    public QuestlineManager() {
        super(GSON, "questlines/questlines");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, Questline> tempQuestlines = new HashMap<>();

        if (!QuestlinesConfig.isDebug.get()) {
            object.entrySet().removeIf(entry -> !entry.getKey().getNamespace().equals("debug"));
        }

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement element = entry.getValue();

            element.getAsJsonObject().addProperty("location", location.toString());

            try {
                Questline questline = GSON.fromJson(element, Questline.class);
                if (questline != null) {
                    tempQuestlines.put(questline.getLocation(), questline);
                } else {
                    LOGGER.error("Could not deserialize questline {}", location);
                }
            } catch (Exception e) {
                LOGGER.error("Could not deserialize questline {} because: {}", location, e.getMessage());
                throw e;
            }

            LOGGER.info("Loaded questline {} questlines", tempQuestlines.size());
            questlines = ImmutableMap.copyOf(tempQuestlines);
        }
    }

    public void applyQuests(List<Quest> quests) {
        for (Quest quest : quests) {
            ResourceLocation questlineLocation = quest.getQuestlineLocation();
            Questline questline = questlines.get(questlineLocation);

            if (questline != null) {
                questline.setStartQuest(quest);
            } else {
                LOGGER.error("Could not find questline {} for quest {}", questlineLocation, quest.getLocation());
            }
        }
    }

    public List<Questline> getQuestlines() {
        return new ArrayList<>(questlines.values());
    }

    public List<Questline> getStartQuestlines() {
        return questlines.values().stream()
                .filter(Questline::isUnlockedByDefault)
                .toList();
    }

    public Optional<Questline> getQuestline(ResourceLocation location) {
        return Optional.ofNullable(questlines.get(location));
    }

    public List<Quest> getQuests() {
        List<Quest> quests = new ArrayList<>();
        for (Questline questline : questlines.values()) {
            List<Quest> questlineQuests = questline.getAllQuests();
            if (questlineQuests != null) {
                quests.addAll(questlineQuests);
            }
        }
        return quests;
    }

    public void replaceQuestlines(List<Questline> questlines) {
        ImmutableMap.Builder<ResourceLocation, Questline> map = ImmutableMap.builder();
        for (Questline questline : questlines) {
            Questlines.getInstance().getQuestManager().getStartQuestFor(questline)
                    .ifPresent(questline::setStartQuest);
            map.put(questline.getLocation(), questline);
        }
        this.questlines = map.build();
        LOGGER.info("Updated {} questlines", this.questlines.size());
    }

    public List<String> getLocationStrings() {
        return questlines.keySet().stream().map(ResourceLocation::toString).toList();
    }
}
