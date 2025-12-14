package com.daqem.questlines.data;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.questline.Questline;
import com.daqem.questlines.questline.quest.Quest;
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

public class QuestlineManager extends SimplePreparableReloadListener<List<Questline>> {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Questline.class, new Questline.Serializer())
            .create();

    private static QuestlineManager instance;
    protected ImmutableMap<ResourceLocation, Questline> questlines = ImmutableMap.of();

    public QuestlineManager() {
        instance = this;
    }

    @Override
    protected @NotNull List<Questline> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, Resource> resourceMap = resourceManager.listResources("questlines/questlines", (resourceLocation) ->
                        resourceLocation.getPath().endsWith(".json")).entrySet().stream()
                .collect(Collectors.toMap(entry ->
                                ResourceLocation.fromNamespaceAndPath(
                                        entry.getKey().getNamespace(),
                                        entry.getKey().getPath()
                                                .substring(0, entry.getKey().getPath().length() - ".json".length())
                                                .substring("questlines/questlines/".length())),
                        Map.Entry::getValue));

        Map<ResourceLocation, JsonObject> map = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : resourceMap.entrySet()) {
            ResourceLocation location = entry.getKey();
            try {
                JsonObject jsonElement = GsonHelper.parse(entry.getValue().openAsReader());
                map.put(location, jsonElement);
            } catch (Exception runtimeException) {
                Questlines.LOGGER.error("Parsing error loading questline {}", location, runtimeException);
            }
        }

        try {
            Path configDir = YamlConfigExpectPlatform.getConfigDirectory().resolve(Questlines.MOD_ID).resolve("questlines");
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
                                Questlines.LOGGER.error("Parsing error loading questline from config {}", path, e);
                            }
                        });
            }
        } catch (Exception e) {
            Questlines.LOGGER.error("Error loading questlines from config", e);
        }

        List<Questline> questlines = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonObject> entry : map.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonObject jsonObject = entry.getValue();
            jsonObject.addProperty("location", location.toString());
            try {
                Questline questline = GSON.fromJson(entry.getValue(), Questline.class);
                questlines.add(questline);
            } catch (JsonParseException | IllegalArgumentException runtimeException) {
                Questlines.LOGGER.error("Parsing error loading questline {}", location, runtimeException);
            }
        }

        return questlines;
    }

    @Override
    protected void apply(List<Questline> questlines, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Questlines.LOGGER.info("Loaded {} questlines", questlines.size());
        this.questlines = questlines.stream()
                .collect(ImmutableMap.toImmutableMap(
                        Questline::getLocation,
                        questline -> questline
                ));
    }

    public static QuestlineManager getInstance() {
        return instance != null ? instance : new QuestlineManager();
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

    public void applyQuests(List<Quest> quests) {
        for (Quest quest : quests) {
            ResourceLocation questlineLocation = quest.getQuestlineLocation();
            Questline questline = questlines.get(questlineLocation);

            if (questline != null) {
                questline.setStartQuest(quest);
            } else {
                Questlines.LOGGER.error("Could not find questline {} for quest {}", questlineLocation, quest.getLocation());
            }
        }
    }

    public void setQuestlines(List<Questline> questlines) {
        this.questlines = questlines.stream()
                .peek(questline ->
                        QuestManager.getInstance().getStartQuestFor(questline).ifPresent(questline::setStartQuest))
                .collect(ImmutableMap.toImmutableMap(
                        Questline::getLocation,
                        itemRestriction -> itemRestriction
                ));
    }

    public List<String> getLocationStrings() {
        return questlines.keySet().stream().map(ResourceLocation::toString).toList();
    }
}
