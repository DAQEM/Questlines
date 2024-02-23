package com.daqem.questlines.questline;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.Quest;
import com.daqem.questlines.questline.quest.QuestProgress;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.daqem.uilib.api.client.gui.component.advancement.IAdvancement;
import com.daqem.uilib.api.client.gui.component.advancement.IAdvancementTree;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
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

public class QuestlineProgress implements ISerializable<QuestlineProgress>, IAdvancementTree {

    private final Questline questline;
    private @Nullable QuestProgress startQuestProgress;

    public QuestlineProgress(Questline questline) {
        this.questline = questline;
    }

    public QuestlineProgress(Questline questline, @Nullable QuestProgress startQuestProgress) {
        this.questline = questline;
        this.startQuestProgress = startQuestProgress;
    }

    public static Optional<QuestlineProgress> findQuestlineProgress(List<QuestlineProgress> questlines1201$questlines, QuestProgress questProgress) {
        return questlines1201$questlines.stream()
                .filter(questlineProgress -> questlineProgress.getAllQuestProgresses().contains(questProgress))
                .findFirst();
    }

    public static List<Quest> findQuestsForParent(QuestlineProgress questlineProgress, QuestProgress questProgress) {
        return questlineProgress.getQuestline().getAllQuests().stream()
                .filter(quest -> quest.getParent().isPresent() && quest.getParent().get().equals(questProgress.getQuest()))
                .toList();
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
        for (QuestProgress child : questProgress.getQuestChildren()) {
            questProgresses.addAll(getAllQuestProgresses(child));
        }
        return questProgresses;
    }

    public List<IActionHolder> getAllActionHolders() {
        return getAllQuestProgresses().stream()
                .flatMap(questProgress -> questProgress.getObjectives().stream())
                .map(ObjectiveProgress::getObjective)
                .collect(Collectors.toList());
    }

    public @Nullable QuestProgress getStartQuestProgress() {
        return startQuestProgress;
    }

    public void setStartQuestProgress(@Nullable QuestProgress startQuestProgress) {
        this.startQuestProgress = startQuestProgress;
    }

    @Override
    public Optional<IAdvancement> getRoot() {
        return Optional.ofNullable(startQuestProgress);
    }

    @Override
    public ResourceLocation getBackgroundTexture() {
        return new ResourceLocation("textures/gui/advancements/backgrounds/stone.png");
    }

    @Override
    public ItemStack getIcon() {
        return Items.ACACIA_PLANKS.getDefaultInstance();
    }

    @Override
    public Component getName() {
        return this.questline.getName();
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
            Questline questline = Questlines.getInstance().getQuestlineManager().getQuestline(friendlyByteBuf.readResourceLocation()).orElse(null);
            boolean hasStartQuestProgress = friendlyByteBuf.readBoolean();
            QuestProgress startQuestProgress = null;
            if (hasStartQuestProgress) {
                startQuestProgress = new QuestProgress.Serializer().fromNetwork(friendlyByteBuf);
            }
            return new QuestlineProgress(questline, startQuestProgress);
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, QuestlineProgress type) {
            friendlyByteBuf.writeResourceLocation(type.getQuestline().getLocation());
            friendlyByteBuf.writeBoolean(type.getStartQuestProgress() != null);
            if (type.getStartQuestProgress() != null) {
                type.getStartQuestProgress().getSerializer().toNetwork(friendlyByteBuf, type.getStartQuestProgress());
            }
        }

        private static final String QUESTS_TAG = "Quests";

        @Override
        public QuestlineProgress fromNBT(CompoundTag compoundTag, ResourceLocation location) {
            Optional<Questline> optionalQuestline = Questlines.getInstance().getQuestlineManager().getQuestline(location);
            if (optionalQuestline.isEmpty())  {
                return null;
            }
            Questline questline = optionalQuestline.get();

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
