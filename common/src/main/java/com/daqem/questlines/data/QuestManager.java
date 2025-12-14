package com.daqem.questlines.data;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.questline.Questline;
import com.daqem.questlines.questline.quest.Quest;
import com.daqem.questlines.questline.quest.QuestProgress;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.daqem.yamlconfig.YamlConfigExpectPlatform;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestManager extends SimplePreparableReloadListener<List<Quest>> {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Quest.class, new Quest.Serializer())
            .create();

    private static QuestManager instance;
    private ImmutableMap<ResourceLocation, Quest> quests = ImmutableMap.of();

    public QuestManager() {
        instance = this;
    }

    @Override
    protected @NotNull List<Quest> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, Resource> resourceMap = resourceManager.listResources("questlines/quests", (resourceLocation) ->
                        resourceLocation.getPath().endsWith(".json")).entrySet().stream()
                .collect(Collectors.toMap(entry ->
                                ResourceLocation.fromNamespaceAndPath(
                                        entry.getKey().getNamespace(),
                                        entry.getKey().getPath()
                                                .substring(0, entry.getKey().getPath().length() - ".json".length())
                                                .substring("questlines/quests/".length())),
                        Map.Entry::getValue));

        Map<ResourceLocation, JsonObject> map = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : resourceMap.entrySet()) {
            ResourceLocation location = entry.getKey();
            try {
                JsonObject jsonElement = GsonHelper.parse(entry.getValue().openAsReader());
                map.put(location, jsonElement);
            } catch (Exception runtimeException) {
                Questlines.LOGGER.error("Parsing error loading quest {}", location, runtimeException);
            }
        }

        try {
            Path configDir = YamlConfigExpectPlatform.getConfigDirectory().resolve(Questlines.MOD_ID).resolve("quests");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            try (Stream<Path> paths = Files.walk(configDir)) {
                paths.filter(path -> path.toString().endsWith(".json"))
                        .forEach(path -> {
                            try (BufferedReader reader = Files.newBufferedReader(path)) {
                                JsonObject jsonElement = GsonHelper.parse(reader);
                                String relativePath = configDir.relativize(path).toString();
                                relativePath = relativePath.replace("\\", "/");
                                relativePath = relativePath.substring(0, relativePath.length() - ".json".length());
                                String namespace;
                                String resourcePath;
                                int firstSlashIndex = relativePath.indexOf('/');
                                if (firstSlashIndex > 0) {
                                    namespace = relativePath.substring(0, firstSlashIndex);
                                    resourcePath = relativePath.substring(firstSlashIndex + 1);
                                } else {
                                    namespace = Questlines.MOD_ID;
                                    resourcePath = relativePath;
                                }
                                ResourceLocation location = ResourceLocation.fromNamespaceAndPath(namespace, resourcePath);
                                map.put(location, jsonElement);
                            } catch (Exception e) {
                                Questlines.LOGGER.error("Parsing error loading quest from config {}", path, e);
                            }
                        });
            }
        } catch (Exception e) {
            Questlines.LOGGER.error("Error loading quests from config", e);
        }

        List<Quest> quests = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonObject> entry : map.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonObject jsonObject = entry.getValue();
            jsonObject.addProperty("location", location.toString());
            try {
                Quest quest = GSON.fromJson(entry.getValue(), Quest.class);
                quests.add(quest);
            } catch (JsonParseException | IllegalArgumentException runtimeException) {
                Questlines.LOGGER.error("Parsing error loading quest {}", location, runtimeException);
            }
        }

        return quests;
    }

    @Override
    protected void apply(List<Quest> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Questlines.LOGGER.info("Loaded {} quests", object.size());
        this.quests = object.stream()
                .collect(ImmutableMap.toImmutableMap(
                        Quest::getLocation,
                        quest -> quest
                ));
        QuestlineManager.getInstance().applyQuests(sortQuests(quests.values().stream().toList()));
    }

    public static QuestManager getInstance() {
        return instance != null ? instance : new QuestManager();
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

    public void setQuests(List<Quest> quests) {
        this.quests = quests.stream()
                .collect(ImmutableMap.toImmutableMap(
                        Quest::getLocation,
                        quest -> quest
                ));
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
                .filter(quest -> quest.getQuestParent() == null)
                .toList();
    }
}
