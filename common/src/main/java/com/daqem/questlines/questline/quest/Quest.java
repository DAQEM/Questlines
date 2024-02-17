package com.daqem.questlines.questline.quest;

import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.registry.ArcRegistry;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Quest {

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

    public @Nullable Quest getParent() {
        return parent;
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
            return null;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, Quest type) {

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
