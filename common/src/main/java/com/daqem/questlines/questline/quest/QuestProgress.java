package com.daqem.questlines.questline.quest;

import com.daqem.questlines.client.gui.widget.QuestWidget;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.daqem.uilib.api.skilltree.ISkillTreeItem;
import com.daqem.uilib.api.widget.skilltree.ISkillTreeItemWidget;
import com.daqem.uilib.skilltree.AbstractSkillTreeItem;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestProgress extends AbstractSkillTreeItem implements ISerializable<QuestProgress> {

    public static final Codec<QuestProgress> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("quest").forGetter(qp -> qp.quest.getLocation()),
                    ObjectiveProgress.CODEC.listOf().fieldOf("objectives").forGetter(QuestProgress::getObjectives)
            ).apply(instance, QuestProgress::new
            )
    );
    private final Quest quest;
    private final List<ObjectiveProgress> objectives;

    public QuestProgress(Quest quest, List<ObjectiveProgress> objectives) {
        super(quest.getParentLocation() == null, new ArrayList<>());
        this.quest = quest;
        this.objectives = objectives;
    }

    public QuestProgress(ResourceLocation resourceLocation, List<ObjectiveProgress> objectives) {
        super(resourceLocation == null, new ArrayList<>());
        this.quest = QuestManager.getInstance().getQuest(resourceLocation).orElse(null);
        this.objectives = objectives;
    }

    @Override
    public ISkillTreeItemWidget createWidget() {
        return new QuestWidget(this);
    }

    public QuestProgress getQuestParent() {
        return (QuestProgress) getParent();
    }

    public List<QuestProgress> getQuestChildren() {
        // Cast the raw ISkillTreeItem list to QuestProgress list
        List<QuestProgress> questChildren = new ArrayList<>();
        for (ISkillTreeItem child : getChildren()) {
            if (child instanceof QuestProgress qp) {
                questChildren.add(qp);
            }
        }
        return questChildren;
    }

    public void addChild(QuestProgress child) {
        super.addChild(child);
    }

    public ItemStack getIcon() {
        return quest.getIcon();
    }

    public Component getName() {
        return quest.getName();
    }

    public List<Component> getDescription() {
        return quest.getDescription(this);
    }

    public boolean isObtained() {
        return getObjectives().stream().allMatch(ObjectiveProgress::isCompleted);
    }

    public boolean isCompleted() {
        return isObtained();
    }

    public AdvancementType getFrameType() {
        return AdvancementType.CHALLENGE;
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
        public QuestProgress fromNetwork(RegistryFriendlyByteBuf buf) {
            Quest quest = QuestManager.getInstance().getQuest(buf.readResourceLocation()).orElse(null);
            List<ObjectiveProgress> objectives = buf.readList(buf1 ->
                    new ObjectiveProgress.Serializer().fromNetwork((RegistryFriendlyByteBuf) buf1));
            List<QuestProgress> questChildren = buf.readList(buf1 ->
                    new QuestProgress.Serializer().fromNetwork((RegistryFriendlyByteBuf) buf1));

            QuestProgress questProgress = new QuestProgress(quest, objectives);

            for (QuestProgress child : questChildren) {
                questProgress.addChild(child);
            }

            return questProgress;
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf buf, QuestProgress type) {
            buf.writeResourceLocation(type.getQuest().getLocation());
            buf.writeCollection(type.getObjectives(), (buf1, objectiveProgress) ->
                    objectiveProgress.getSerializer().toNetwork((RegistryFriendlyByteBuf) buf1, objectiveProgress));
            buf.writeCollection(type.getQuestChildren(), (buf1, questProgress) ->
                    questProgress.getSerializer().toNetwork((RegistryFriendlyByteBuf) buf1, questProgress));
        }
    }
}