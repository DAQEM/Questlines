package com.daqem.questlines.questline;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.QuestProgress;
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

public class QuestlineProgress implements ISerializable<QuestlineProgress> {

    private final Questline questline;
    private @Nullable QuestProgress startQuestProgress;

    public QuestlineProgress(Questline questline) {
        this.questline = questline;
    }

    public QuestlineProgress(Questline questline, @Nullable QuestProgress startQuestProgress) {
        this.questline = questline;
        this.startQuestProgress = startQuestProgress;
    }

    public Questline getQuestline() {
        return questline;
    }

    public List<QuestProgress> getAllQuestProgresses() {
        if (startQuestProgress == null) {
            return new ArrayList<>();
        }
        return getAllQuestProgresses(startQuestProgress);
    }

    public static List<QuestProgress> getAllQuestProgresses(QuestProgress questProgress) {
        List<QuestProgress> questProgresses = new ArrayList<>();
        if (questProgress == null) {
            return questProgresses;
        }
        questProgresses.add(questProgress);
        for (QuestProgress child : questProgress.getChildren()) {
            questProgresses.addAll(getAllQuestProgresses(child));
        }
        return questProgresses;
    }

    public @Nullable QuestProgress getStartQuestProgress() {
        return startQuestProgress;
    }

    public void setStartQuestProgress(@Nullable QuestProgress startQuestProgress) {
        this.startQuestProgress = startQuestProgress;
    }

    @Override
    public ISerializer<QuestlineProgress> getSerializer() {
        return new Serializer();
    }

    public static class Serializer implements ISerializer<QuestlineProgress> {

        @Override
        public QuestlineProgress deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }

        @Override
        public QuestlineProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return null;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, QuestlineProgress type) {

        }

        private static final String QUESTS_TAG = "Quests";

        @Override
        public QuestlineProgress fromNBT(CompoundTag compoundTag, ResourceLocation location) {
            Questline questline = Questlines.getInstance().getQuestlineManager().getQuestline(location);
            if (questline == null) {
                return null;
            }

            QuestProgress.Serializer serializer = new QuestProgress.Serializer();
            List<QuestProgress> questProgresses = new ArrayList<>();
            CompoundTag questsTag = compoundTag.getCompound(QUESTS_TAG);
            for (String key : questsTag.getAllKeys()) {
                QuestProgress questProgress = serializer.fromNBT(
                        questsTag.getCompound(key),
                        new ResourceLocation(key));

                if (questProgress != null) {
                    questProgresses.add(questProgress);
                }
            }

            questProgresses = QuestManager.sortQuestProgresses(questProgresses);

            return new QuestlineProgress(questline, questProgresses.stream()
                    .findFirst()
                    .orElse(null));
        }

        @Override
        public CompoundTag toNBT(QuestlineProgress questline) {
            CompoundTag tag = new CompoundTag();
            List<QuestProgress> questProgresses = questline.getAllQuestProgresses();
            CompoundTag questsTag = new CompoundTag();
            for (QuestProgress questProgress : questProgresses) {
                questsTag.put(
                        questProgress.getQuest().getLocation().toString(),
                        questProgress.getSerializer().toNBT(questProgress)
                );
            }
            tag.put(QUESTS_TAG, questsTag);
            return tag;
        }
    }
}
