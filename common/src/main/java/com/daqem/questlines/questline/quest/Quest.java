package com.daqem.questlines.questline.quest;

import com.daqem.arc.api.reward.IReward;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

public class Quest {

    private final ResourceLocation location;
    private final @Nullable Quest parent;
    private final List<Quest> children;
    private final List<Objective> objectives;
    private final List<IReward> rewards;

    public Quest(ResourceLocation location, @Nullable Quest parent, List<Quest> children, List<Objective> objectives, List<IReward> rewards) {
        this.location = location;
        this.parent = parent;
        this.children = children;
        this.objectives = objectives;
        this.rewards = rewards;
    }

    public static class Serializer implements ISerializer<Quest> {

        @Override
        public JsonElement serialize(Quest quest, Type type, JsonSerializationContext jsonSerializationContext) {
            return null;
        }

        @Override
        public Quest deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }

        @Override
        public Quest fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return null;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, Quest type) {

        }

        @Override
        public Quest fromNBT(CompoundTag compoundTag) {
            return null;
        }

        @Override
        public CompoundTag toNBT(Quest type) {
            return null;
        }
    }
}
