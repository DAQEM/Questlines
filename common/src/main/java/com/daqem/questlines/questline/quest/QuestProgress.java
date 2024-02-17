package com.daqem.questlines.questline.quest;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestProgress implements ISerializable<QuestProgress> {

    private @Nullable QuestProgress parent;
    private final List<QuestProgress> children = new ArrayList<>();

    private final Quest quest;
    private final List<ObjectiveProgress> objectives;

    public QuestProgress(Quest quest, List<ObjectiveProgress> objectives) {
        this.quest = quest;
        this.objectives = objectives;
    }

    public @Nullable QuestProgress getParent() {
        return parent;
    }

    public void setParent(@Nullable QuestProgress parent) {
        this.parent = parent;
    }

    public List<QuestProgress> getChildren() {
        return children;
    }

    public void addChild(QuestProgress child) {
        children.add(child);
    }

    public Quest getQuest() {
        return quest;
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
            return null;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, QuestProgress type) {

        }

        private static final String OBJECTIVES_TAG = "Objectives";

        @Override
        public QuestProgress fromNBT(CompoundTag compoundTag, ResourceLocation location) {
            Quest quest = Questlines.getInstance().getQuestManager().getQuest(location);
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
