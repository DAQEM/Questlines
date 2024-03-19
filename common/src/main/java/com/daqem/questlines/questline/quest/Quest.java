package com.daqem.questlines.questline.quest;

import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.registry.ArcRegistry;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class Quest implements ISerializable<Quest> {

    private final ResourceLocation location;
    private final ResourceLocation questlineLocation;
    private final @Nullable ResourceLocation parentLocation;

    private final @Nullable String name;
    private final @Nullable String description;
    private final ItemStack icon;

    private @Nullable Quest parent;
    private final List<Quest> children = new ArrayList<>();
    private final Map<ResourceLocation, Objective> objectives;
    private final List<IReward> rewards;

    public Quest(ResourceLocation location, ResourceLocation questlineLocation, @Nullable ResourceLocation parentLocation, @Nullable String name, @Nullable String description, ItemStack icon, Map<ResourceLocation, Objective> objectives, List<IReward> rewards) {
        this.location = location;
        this.questlineLocation = questlineLocation;
        this.parentLocation = parentLocation;
        this.name = name;
        this.description = description;
        this.icon = icon;
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

    public ItemStack getIcon() {
        return icon;
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
        return this.name != null ? Questlines.literal(this.name) : Questlines.translatable("quest." + location.toString().replace(":", ".").replace("/", "."));
    }

    public List<Component> getDescription(QuestProgress progress) {
        List<Component> description = new ArrayList<>(List.of(this.description != null ? Questlines.literal(this.description) : Questlines.translatable("quest." + location.toString().replace(":", ".").replace("/", ".") + ".description")));
        if (!objectives.isEmpty()) {
            description.add(Questlines.literal(" "));
        }
        progress.getObjectives().forEach(objective -> description.add(objective.getName()));

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

            JsonObject iconObject = jsonObject.getAsJsonObject("icon");
            ItemStack icon = iconObject != null ? getItemStack(iconObject,"item") : ItemStack.EMPTY;
            CompoundTag nbt = iconObject != null ? getCompoundTag(iconObject) : null;
            if (nbt != null) {
                icon.setTag(nbt);
            }

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
                    GsonHelper.getAsString(jsonObject, "name", null),
                    GsonHelper.getAsString(jsonObject, "description", null),
                    icon,
                    objectives,
                    rewards
            );
        }

        @Override
        public Quest fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            ResourceLocation location = friendlyByteBuf.readResourceLocation();
            ResourceLocation questlineLocation = friendlyByteBuf.readResourceLocation();
            ResourceLocation parentLocation = friendlyByteBuf.readBoolean() ? friendlyByteBuf.readResourceLocation() : null;
            boolean hasName = friendlyByteBuf.readBoolean();
            String name = hasName ? friendlyByteBuf.readUtf() : null;
            boolean hasDescription = friendlyByteBuf.readBoolean();
            String description = hasDescription ? friendlyByteBuf.readUtf() : null;
            ItemStack icon = friendlyByteBuf.readItem();
            List<Objective> objectives = friendlyByteBuf.readList(new Objective.Serializer()::fromNetwork);
            Map<ResourceLocation, Objective> objectivesMap = objectives.stream().collect(Collectors.toMap(Objective::getLocation, objective -> objective));
            List<IReward> rewards = friendlyByteBuf.readList(IRewardSerializer::fromNetwork);
            return new Quest(location, questlineLocation, parentLocation, name, description, icon, objectivesMap, rewards);
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, Quest type) {
            friendlyByteBuf.writeResourceLocation(type.location);
            friendlyByteBuf.writeResourceLocation(type.questlineLocation);

            boolean hasParentLocation = type.parentLocation != null;
            friendlyByteBuf.writeBoolean(hasParentLocation);
            if (hasParentLocation) {
                friendlyByteBuf.writeResourceLocation(type.parentLocation);
            }

            boolean hasName = type.name != null;
            friendlyByteBuf.writeBoolean(hasName);
            if (hasName) {
                friendlyByteBuf.writeUtf(type.name);
            }

            boolean hasDescription = type.description != null;
            friendlyByteBuf.writeBoolean(hasDescription);
            if (hasDescription) {
                friendlyByteBuf.writeUtf(type.description);
            }

            friendlyByteBuf.writeItem(type.icon);
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
