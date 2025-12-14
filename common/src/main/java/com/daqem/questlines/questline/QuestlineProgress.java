package com.daqem.questlines.questline;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.QuestlineManager;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.Quest;
import com.daqem.questlines.questline.quest.QuestProgress;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.daqem.uilib.api.skilltree.ISkillTreeItem;
import com.daqem.uilib.skilltree.AbstractSkillTree;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuestlineProgress extends AbstractSkillTree implements ISerializable<QuestlineProgress> {

    public static final Codec<QuestlineProgress> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("questline").forGetter(qlp -> qlp.questline.getLocation()),
                    QuestProgress.CODEC.listOf().fieldOf("quest_progresses").forGetter(QuestlineProgress::getAllQuestProgresses)
            ).apply(instance, (questline, questProgresses) -> {
                QuestProgress startQuestProgress = QuestManager.sortQuestProgresses(questProgresses).stream().findFirst().orElse(null);
                return new QuestlineProgress(questline, startQuestProgress);
            }

            )
    );
    private final Questline questline;
    private @Nullable QuestProgress startQuestProgress;

    public QuestlineProgress(Questline questline) {
        super(new ArrayList<>());
        this.questline = questline;
    }

    public QuestlineProgress(Questline questline, @Nullable QuestProgress startQuestProgress) {
        super(getAllItemsFlat(startQuestProgress));
        this.questline = questline;
        this.startQuestProgress = startQuestProgress;
    }

    public QuestlineProgress(ResourceLocation resourceLocation, @Nullable QuestProgress startQuestProgress) {
        super(getAllItemsFlat(startQuestProgress));
        this.questline = QuestlineManager.getInstance().getQuestline(resourceLocation).orElse(null);
        this.startQuestProgress = startQuestProgress;
    }

    private static List<ISkillTreeItem> getAllItemsFlat(@Nullable QuestProgress startNode) {
        if (startNode == null) return new ArrayList<>();
        List<ISkillTreeItem> items = new ArrayList<>();
        collectItems(startNode, items);
        return items;
    }

    private static void collectItems(QuestProgress node, List<ISkillTreeItem> list) {
        list.add(node);
        for (ISkillTreeItem child : node.getChildren()) {
            if (child instanceof QuestProgress qp) {
                collectItems(qp, list);
            }
        }
    }

    public static Optional<QuestlineProgress> findQuestlineProgress(List<QuestlineProgress> questlines, QuestProgress questProgress) {
        return questlines.stream()
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

    public @Nullable QuestProgress getStartQuestProgress() {
        return startQuestProgress;
    }

    public ItemStack getIcon() {
        return questline.getIcon();
    }

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
        public QuestlineProgress fromNetwork(RegistryFriendlyByteBuf buf) {
            Questline questline = QuestlineManager.getInstance().getQuestline(buf.readResourceLocation()).orElse(null);
            boolean hasStartQuestProgress = buf.readBoolean();
            QuestProgress startQuestProgress = null;
            if (hasStartQuestProgress) {
                startQuestProgress = new QuestProgress.Serializer().fromNetwork(buf);
            }
            return new QuestlineProgress(questline, startQuestProgress);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf buf, QuestlineProgress questlineProgress) {
            buf.writeResourceLocation(questlineProgress.getQuestline().getLocation());
            buf.writeBoolean(questlineProgress.getStartQuestProgress() != null);
            if (questlineProgress.getStartQuestProgress() != null) {
                questlineProgress.getStartQuestProgress().getSerializer().toNetwork(buf, questlineProgress.getStartQuestProgress());
            }
        }
    }
}