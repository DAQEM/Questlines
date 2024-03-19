package com.daqem.questlines.questline.quest.objective;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public class ObjectiveProgress implements ISerializable<ObjectiveProgress> {

    private final Objective objective;
    private int progress;

    public ObjectiveProgress(Objective objective) {
        this(objective, 0);
    }

    public ObjectiveProgress(Objective objective, int progress) {
        this.objective = objective;
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
        public ObjectiveProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            int progress = friendlyByteBuf.readInt();
            Objective objective = Questlines.getInstance().getQuestManager().getObjective(friendlyByteBuf.readResourceLocation()).orElse(null);
            return new ObjectiveProgress(objective, progress);
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, ObjectiveProgress type) {
            friendlyByteBuf.writeInt(type.getProgress());
            friendlyByteBuf.writeResourceLocation(type.getObjective().getLocation());
        }

        private static final String PROGRESS = "Progress";

        @Override
        public ObjectiveProgress fromNBT(CompoundTag compoundTag, ResourceLocation location) {
            Objective objective = Questlines.getInstance().getQuestManager().getObjective(location).orElse(null);
            if (objective == null) {
                return null;
            }

            int progress = compoundTag.getInt(PROGRESS);
            return new ObjectiveProgress(objective, progress);
        }

        @Override
        public CompoundTag toNBT(ObjectiveProgress type) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(PROGRESS, type.progress);
            return compoundTag;
        }
    }
}
