package com.daqem.questlines.questline.quest;

import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.registry.ArcRegistry;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.daqem.questlines.questline.Questline;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class Quest implements ISerializable<Quest> {

    private final ResourceLocation location;
    private final ResourceLocation questlineLocation;
    private final @Nullable ResourceLocation parentLocation;

    private @Nullable Quest parent;
    private final List<Quest> children = new ArrayList<>();
    private final Map<ResourceLocation, Objective> objectives;
    private final List<IReward> rewards;

    public Quest(ResourceLocation location, ResourceLocation questlineLocation, @Nullable ResourceLocation parentLocation, Map<ResourceLocation, Objective> objectives, List<IReward> rewards) {
        this.location = location;
        this.questlineLocation = questlineLocation;
        this.parentLocation = parentLocation;
        this.objectives = objectives;
        this.rewards = rewards;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public ResourceLocation getQuestlineLocation() {
        return questlineLocation;
    }

    public @Nullable ResourceLocation getParentLocation() {
        return parentLocation;
    }

    public Optional<Quest> getParent() {
        return Optional.ofNullable(parent);
    }

    public void setParent(@Nullable Quest parent) {
        this.parent = parent;
    }

    public List<Quest> getChildren() {
        return children;
    }

    public void addChild(Quest child) {
        children.add(child);
    }

    public List<Objective> getObjectives() {
        return objectives.values().stream().toList();
    }

    public List<IReward> getRewards() {
        return rewards;
    }

    public Component getName() {
        return Questlines.translatable("quest." + location.toString().replace(":", ".").replace("/", "."));
    }

    public List<Component> getDescription(QuestProgress progress) {
        List<Component> description = new ArrayList<>(List.of(Questlines.translatable("quest." + location.toString().replace(":", ".").replace("/", ".") + ".description")));
        if (!objectives.isEmpty()) {
            description.add(Questlines.literal(" "));
        }
        progress.getObjectives().forEach(objective -> description.add(objective.getDescription()));

        return description;
    }

    public List<ObjectiveProgress> createObjectiveProgresses() {
        return objectives.values().stream()
                .map(ObjectiveProgress::new)
                .toList();
    }

    public QuestProgress createQuestProgress() {
        return new QuestProgress(this, createObjectiveProgresses());
    }

    @Override
    public ISerializer<Quest> getSerializer() {
        return new Serializer();
    }

    public static class Serializer implements ISerializer<Quest> {

        private static final Gson GSON = new GsonBuilder()
                .registerTypeHierarchyAdapter(Objective.class, new Objective.Serializer())
                .create();

        @Override
        public Quest deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String parentLocation = GsonHelper.getAsString(jsonObject, "parent", "");

            ResourceLocation location = getResourceLocation(jsonObject, "location");
            Map<ResourceLocation, Objective> objectives = new HashMap<>();
            List<IReward> rewards = new ArrayList<>();

            if (jsonObject.has("objectives")) {
                jsonObject.getAsJsonArray("objectives").forEach(json -> {
                    try {
                        json.getAsJsonObject().addProperty("location", location.toString());
                        Objective objective = GSON.fromJson(json, Objective.class);
                        if (objective != null) {
                            objectives.put(objective.getLocation(), objective);
                            ActionHolderManager.getInstance().registerActionHolder(objective);
                        }
                    } catch (Exception e) {
                        QuestManager.LOGGER.error("Could not deserialize objective of {} because: {}", location.toString(), e.getMessage());
                    }
                });
            }


            if (jsonObject.has("rewards")) {
                jsonObject.getAsJsonArray("rewards").forEach(json1 -> {
                    ResourceLocation rewardLocation = getResourceLocation(json1.getAsJsonObject(), "type");
                    ArcRegistry.REWARD_SERIALIZER.getOptional(rewardLocation).ifPresent(serializer -> {
                        rewards.add(serializer.fromJson(location, json1.getAsJsonObject()));
                    });
                });
            }

            return new Quest(
                    location,
                    getResourceLocation(jsonObject, "questline"),
                    parentLocation.isEmpty() ? null : new ResourceLocation(parentLocation),
                    objectives,
                    rewards
            );
        }

        @Override
        public Quest fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            ResourceLocation location = friendlyByteBuf.readResourceLocation();
            ResourceLocation questlineLocation = friendlyByteBuf.readResourceLocation();
            ResourceLocation parentLocation = friendlyByteBuf.readBoolean() ? friendlyByteBuf.readResourceLocation() : null;
            List<Objective> objectives = friendlyByteBuf.readList(new Objective.Serializer()::fromNetwork);
            Map<ResourceLocation, Objective> objectivesMap = objectives.stream().collect(Collectors.toMap(Objective::getLocation, objective -> objective));
            List<IReward> rewards = friendlyByteBuf.readList(IRewardSerializer::fromNetwork);
            return new Quest(location, questlineLocation, parentLocation, objectivesMap, rewards);
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, Quest type) {
            friendlyByteBuf.writeResourceLocation(type.location);
            friendlyByteBuf.writeResourceLocation(type.questlineLocation);
            friendlyByteBuf.writeBoolean(type.parentLocation != null);
            if (type.parentLocation != null) {
                friendlyByteBuf.writeResourceLocation(type.parentLocation);
            }
            friendlyByteBuf.writeCollection(type.getObjectives(),
                    (friendlyByteBuf1, objective) -> new Objective.Serializer().toNetwork(friendlyByteBuf1, objective));
            friendlyByteBuf.writeCollection(type.getRewards(),
                    (friendlyByteBuf1, reward) -> IRewardSerializer.toNetwork(reward, friendlyByteBuf1, reward.getType().getLocation()));
        }

        @Override
        public Quest fromNBT(CompoundTag compoundTag, ResourceLocation location) {
            return null;
        }

        @Override
        public CompoundTag toNBT(Quest type) {
            return null;
        }
    }
}
