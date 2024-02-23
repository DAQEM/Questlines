package com.daqem.questlines.questline;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.Quest;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Questline implements ISerializable<Questline> {

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

    public Optional<Quest> getStartQuest() {
        return Optional.ofNullable(startQuest);
    }

    public void setStartQuest(@Nullable Quest startQuest) {
        this.startQuest = startQuest;
    }

    public List<Quest> getAllQuests() {
        if (startQuest == null) {
            return new ArrayList<>();
        }
        return getAllQuests(startQuest);
    }

    public static List<Quest> getAllQuests(Quest quest) {
        List<Quest> quests = new ArrayList<>();
        if (quest == null) {
            return quests;
        }
        quests.add(quest);
        for (Quest child : quest.getChildren()) {
            quests.addAll(getAllQuests(child));
        }
        return quests;
    }

    public boolean isUnlockedByDefault() {
        return isUnlockedByDefault;
    }

    @Override
    public ISerializer<Questline> getSerializer() {
        return new Serializer();
    }

    public Component getName() {
        return Questlines.translatable(location.toString().replace(":", ".").replace("/", "."));
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
            return new Questline(
                    friendlyByteBuf.readResourceLocation(),
                    friendlyByteBuf.readBoolean()
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, Questline type) {
            friendlyByteBuf.writeResourceLocation(type.getLocation());
            friendlyByteBuf.writeBoolean(type.isUnlockedByDefault());
        }

        @Override
        public Questline fromNBT(CompoundTag compoundTag, ResourceLocation location) {
            return null;
        }

        @Override
        public CompoundTag toNBT(Questline type) {
            return null;
        }
    }
}
