package com.daqem.questlines.questline.quest;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.daqem.uilib.api.client.gui.component.advancement.IAdvancement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.FrameType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuestProgress implements ISerializable<QuestProgress>, IAdvancement {

    private @Nullable QuestProgress parent;
    private final List<QuestProgress> children = new ArrayList<>();

    private final Quest quest;
    private final List<ObjectiveProgress> objectives;

    public QuestProgress(Quest quest, List<ObjectiveProgress> objectives) {
        this.quest = quest;
        this.objectives = objectives;
    }

    public Optional<IAdvancement> getParent() {
        return Optional.ofNullable(parent);
    }

    public Optional<QuestProgress> getQuestParent() {
        return Optional.ofNullable(parent);
    }

    public void setParent(@Nullable QuestProgress parent) {
        this.parent = parent;
    }

    public List<IAdvancement> getChildren() {
        return new ArrayList<>(children);
    }

    public List<QuestProgress> getQuestChildren() {
        return new ArrayList<>(children);
    }

    @Override
    public void addChild(IAdvancement advancement) {
        if (advancement instanceof QuestProgress && !children.contains(advancement)) {
            children.add((QuestProgress) advancement);
        }
    }

    @Override
    public ItemStack getIcon() {
        return quest.getIcon();
    }

    @Override
    public Component getName() {
        return quest.getName();
    }

    @Override
    public List<Component> getDescription() {
        return quest.getDescription(this);
    }

    @Override
    public boolean isObtained() {
        return getObjectives().stream().allMatch(ObjectiveProgress::isCompleted);
    }

    public boolean isCompleted() {
        return isObtained();
    }

    @Override
    public FrameType getFrameType() {
        return FrameType.CHALLENGE;
    }

    public void addChild(QuestProgress child) {
        children.add(child);
    }

    public Quest getQuest() {
        return quest;
    }

    public List<ObjectiveProgress> getObjectives() {
        return objectives;
    }

    @Override
    public ISerializer<QuestProgress> getSerializer() {
        return new Serializer();
    }

    public static class Serializer implements ISerializer<QuestProgress> {

        @Override
        public QuestProgress deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }

        @Override
        public QuestProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            Quest quest = Questlines.getInstance().getQuestManager().getQuest(friendlyByteBuf.readResourceLocation()).orElse(null);
            List<ObjectiveProgress> objectives = friendlyByteBuf.readList(friendlyByteBuf1 ->
                    new ObjectiveProgress.Serializer().fromNetwork(friendlyByteBuf1));
            List<QuestProgress> questChildren = friendlyByteBuf.readList(friendlyByteBuf1 ->
                    new QuestProgress.Serializer().fromNetwork(friendlyByteBuf1));

            QuestProgress questProgress = new QuestProgress(quest, objectives);

            for (QuestProgress child : questChildren) {
                questProgress.addChild(child);
            }

            return questProgress;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, QuestProgress type) {
            friendlyByteBuf.writeResourceLocation(type.getQuest().getLocation());
            friendlyByteBuf.writeCollection(type.getObjectives(), (friendlyByteBuf1, objectiveProgress) ->
                    objectiveProgress.getSerializer().toNetwork(friendlyByteBuf1, objectiveProgress));
            friendlyByteBuf.writeCollection(type.getQuestChildren(), (friendlyByteBuf1, questProgress) ->
                    questProgress.getSerializer().toNetwork(friendlyByteBuf1, questProgress));
        }

        private static final String OBJECTIVES_TAG = "Objectives";

        @Override
        public QuestProgress fromNBT(CompoundTag compoundTag, ResourceLocation location) {
            Quest quest = Questlines.getInstance().getQuestManager().getQuest(location).orElse(null);
            if (quest == null) {
                return null;
            }

            ObjectiveProgress.Serializer serializer = new ObjectiveProgress.Serializer();
            List<ObjectiveProgress> objectives = new ArrayList<>();
            CompoundTag objectivesTag = compoundTag.getCompound(OBJECTIVES_TAG);
            for (String key : objectivesTag.getAllKeys()) {
                ObjectiveProgress objectiveProgress = serializer.fromNBT(
                        objectivesTag.getCompound(key),
                        new ResourceLocation(key));

                if (objectiveProgress != null) {
                    objectives.add(objectiveProgress);
                }
            }

            return new QuestProgress(quest, objectives);
        }

        @Override
        public CompoundTag toNBT(QuestProgress type) {
            CompoundTag tag = new CompoundTag();
            CompoundTag objectivesTag = new CompoundTag();
            for (ObjectiveProgress objectiveProgress : type.objectives) {
                objectivesTag.put(
                        objectiveProgress.getObjective().getLocation().toString(),
                        objectiveProgress.getSerializer().toNBT(objectiveProgress)
                );
            }
            tag.put(OBJECTIVES_TAG, objectivesTag);
            return tag;
        }
    }
}
