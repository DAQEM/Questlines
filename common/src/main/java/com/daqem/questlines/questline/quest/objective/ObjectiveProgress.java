package com.daqem.questlines.questline.quest.objective;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public class ObjectiveProgress implements ISerializable<ObjectiveProgress> {

    private final Objective objective;
    private int progress;

    public ObjectiveProgress(Objective objective, int progress) {
        this.objective = objective;
        this.progress = progress;
    }

    public Objective getObjective() {
        return objective;
    }

    @Override
    public ISerializer<ObjectiveProgress> getSerializer() {
        return new Serializer();
    }

    public static class Serializer implements ISerializer<ObjectiveProgress> {

        @Override
        public ObjectiveProgress deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }

        @Override
        public ObjectiveProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return null;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, ObjectiveProgress type) {

        }

        private static final String PROGRESS = "Progress";

        @Override
        public ObjectiveProgress fromNBT(CompoundTag compoundTag, ResourceLocation location) {
            Objective objective = Questlines.getInstance().getQuestManager().getObjective(location);
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
