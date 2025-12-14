package com.daqem.questlines.questline.quest;

import com.daqem.arc.api.action.holder.IActionHolderSerializer;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.IRewardSerializer;
import com.daqem.arc.data.ActionHolderManager;
import com.daqem.arc.registry.ArcRegistry;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

            if (jsonObject.has("objectives")) {
                jsonObject.getAsJsonArray("objectives").forEach(json -> {
                    try {
                        json.getAsJsonObject().addProperty("location", location.toString());
                        Objective objective = GSON.fromJson(json, Objective.class);
                        if (objective != null) {
                            objectives.put(objective.getLocation(), objective);
                            ActionHolderManager.getInstance().registerActionHolders(List.of(objective));
                        }
                    } catch (Exception e) {
                        Questlines.LOGGER.error("Could not deserialize objective of {} because: {}", location.toString(), e.getMessage());
                    }
                });
            }


            if (jsonObject.has("rewards")) {
                jsonObject.getAsJsonArray("rewards").forEach(json1 -> {
                    ResourceLocation rewardLocation = getResourceLocation(json1.getAsJsonObject(), "type");
                    ArcRegistry.REWARD.getOptional(rewardLocation).ifPresent(rewardType -> {
                        rewards.add(rewardType.getSerializer().fromJson(location, json1.getAsJsonObject()));
                    });
                });
            }

            return new Quest(
                    location,
                    getResourceLocation(jsonObject, "questline"),
                    parentLocation.isEmpty() ? null : ResourceLocation.parse(parentLocation),
                    GsonHelper.getAsString(jsonObject, "name", null),
                    GsonHelper.getAsString(jsonObject, "description", null),
                    getItemStack(jsonObject, "icon", ItemStack.EMPTY),
                    objectives,
                    rewards
            );
        }

        @Override
        public Quest fromNetwork(RegistryFriendlyByteBuf buf) {
            ResourceLocation location = buf.readResourceLocation();
            ResourceLocation questlineLocation = buf.readResourceLocation();
            ResourceLocation parentLocation = buf.readBoolean() ? buf.readResourceLocation() : null;
            boolean hasName = buf.readBoolean();
            String name = hasName ? buf.readUtf() : null;
            boolean hasDescription = buf.readBoolean();
            String description = hasDescription ? buf.readUtf() : null;
            ItemStack icon = ItemStack.STREAM_CODEC.decode(buf);
            List<Objective> objectives = buf.readList(buf1 -> (Objective) IActionHolderSerializer.fromNetwork((RegistryFriendlyByteBuf) buf1));
            Map<ResourceLocation, Objective> objectivesMap = objectives.stream().collect(Collectors.toMap(Objective::getLocation, objective -> objective));
            List<IReward> rewards = buf.readList(buf1 -> IRewardSerializer.fromNetwork((RegistryFriendlyByteBuf) buf1));
            return new Quest(location, questlineLocation, parentLocation, name, description, icon, objectivesMap, rewards);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf buf, Quest quest) {
            buf.writeResourceLocation(quest.location);
            buf.writeResourceLocation(quest.questlineLocation);

            boolean hasParentLocation = quest.parentLocation != null;
            buf.writeBoolean(hasParentLocation);
            if (hasParentLocation) {
                buf.writeResourceLocation(quest.parentLocation);
            }

            boolean hasName = quest.name != null;
            buf.writeBoolean(hasName);
            if (hasName) {
                buf.writeUtf(quest.name);
            }

            boolean hasDescription = quest.description != null;
            buf.writeBoolean(hasDescription);
            if (hasDescription) {
                buf.writeUtf(quest.description);
            }

            ItemStack.STREAM_CODEC.encode(buf, quest.icon);
            buf.writeCollection(quest.getObjectives(),
                    (buf1, objective) -> IActionHolderSerializer.toNetwork(objective, (RegistryFriendlyByteBuf) buf1));
            buf.writeCollection(quest.getRewards(),
                    (buf1, reward) -> IRewardSerializer.toNetwork(reward, (RegistryFriendlyByteBuf) buf1, reward.getType().getLocation()));
        }
    }
}
