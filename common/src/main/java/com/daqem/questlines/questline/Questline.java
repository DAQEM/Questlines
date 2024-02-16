package com.daqem.questlines.questline;

import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.Quest;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public class Questline {

    private final ResourceLocation location;
    private final Quest startQuest;

    public Questline(ResourceLocation location, Quest startQuest) {
        this.location = location;
        this.startQuest = startQuest;
    }

    public static class Serializer implements ISerializer<Questline> {

        @Override
        public JsonElement serialize(Questline questline, Type type, JsonSerializationContext jsonSerializationContext) {
            return null;
        }

        @Override
        public Questline deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }

        @Override
        public Questline fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return null;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, Questline type) {

        }

        @Override
        public Questline fromNBT(CompoundTag compoundTag) {
            return null;
        }

        @Override
        public CompoundTag toNBT(Questline type) {
            return null;
        }
    }
}
