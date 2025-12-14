package com.daqem.questlines.questline.quest.objective;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public class ObjectiveProgress implements ISerializable<ObjectiveProgress> {

    public static final Codec<ObjectiveProgress> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("objective").forGetter(op -> op.objective.getLocation()),
                    Codec.INT.fieldOf("progress").forGetter(ObjectiveProgress::getProgress)
            ).apply(instance, ObjectiveProgress::new)
    );
    private final Objective objective;
    private int progress;

    public ObjectiveProgress(Objective objective) {
        this(objective, 0);
    }

    public ObjectiveProgress(Objective objective, int progress) {
        this.objective = objective;
        this.progress = progress;
    }

    public ObjectiveProgress(ResourceLocation resourceLocation, int progress) {
        this.objective = QuestManager.getInstance().getObjective(resourceLocation).orElse(null);
        this.progress = progress;
    }

    public Objective getObjective() {
        return objective;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (this.progress < objective.getGoal()) {
            this.progress = Math.min(progress, objective.getGoal());
            if (this.progress == objective.getGoal()) {
                Questlines.LOGGER.info("Objective " + objective.getLocation() + " completed!");
            }
        }

        if (this.progress > objective.getGoal()) {
            this.progress = objective.getGoal();
        }

        if (this.progress < 0) {
            this.progress = 0;
        }
    }

    public void addProgress(int progress) {
        setProgress(getProgress() + progress);
    }

    @Override
    public ISerializer<ObjectiveProgress> getSerializer() {
        return new Serializer();
    }

    public boolean isCompleted() {
        return progress >= objective.getGoal();
    }

    public Component getName() {
        return objective.getName(this);
    }

    public static class Serializer implements ISerializer<ObjectiveProgress> {

        @Override
        public ObjectiveProgress deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }

        @Override
        public ObjectiveProgress fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf) {
            int progress = friendlyByteBuf.readInt();
            Objective objective = QuestManager.getInstance().getObjective(friendlyByteBuf.readResourceLocation()).orElse(null);
            return new ObjectiveProgress(objective, progress);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ObjectiveProgress type) {
            friendlyByteBuf.writeInt(type.getProgress());
            friendlyByteBuf.writeResourceLocation(type.getObjective().getLocation());
        }
    }
}
