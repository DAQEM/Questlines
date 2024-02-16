package com.daqem.questlines.questline;

import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.Quest;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class Questline {

    private final ResourceLocation location;
    private @Nullable Quest startQuest;

    private final boolean isUnlockedByDefault;

    public Questline(ResourceLocation location, boolean isUnlockedByDefault) {
        this.location = location;
        this.isUnlockedByDefault = isUnlockedByDefault;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public void setStartQuest(@Nullable Quest startQuest) {
        this.startQuest = startQuest;
    }

    public static class Serializer implements ISerializer<Questline> {

        @Override
        public Questline deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return new Questline(
                    getResourceLocation(jsonObject, "location"),
                    GsonHelper.getAsBoolean(jsonObject, "isUnlockedByDefault", true)
            );
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
