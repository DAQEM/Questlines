package com.daqem.questlines.questline;

import com.daqem.arc.data.serializer.ArcSerializer;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.questline.quest.Quest;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Questline implements ISerializable<Questline> {

    private final ResourceLocation location;
    private final @Nullable String name;
    private final ItemStack icon;
    private @Nullable Quest startQuest;

    private final boolean isUnlockedByDefault;

    public Questline(ResourceLocation location, @Nullable String name, ItemStack icon, boolean isUnlockedByDefault) {
        this.location = location;
        this.name = name;
        this.icon = icon;
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
        return this.name != null ? Questlines.literal(this.name) : Questlines.translatable("questline." + location.toString().replace(":", ".").replace("/", "."));
    }

    public ItemStack getIcon() {
        return icon;
    }

    public static class Serializer implements ISerializer<Questline> {

        @Override
        public Questline deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return new Questline(
                    getResourceLocation(jsonObject, "location"),
                    GsonHelper.getAsString(jsonObject, "name", null),
                    getItemStack(jsonObject, "icon", ItemStack.EMPTY),
                    GsonHelper.getAsBoolean(jsonObject, "isUnlockedByDefault", true)
            );
        }

        @Override
        public Questline fromNetwork(RegistryFriendlyByteBuf buf) {
            ResourceLocation location = buf.readResourceLocation();
            boolean hasName = buf.readBoolean();
            String name = hasName ? buf.readUtf() : null;
            ItemStack icon = ItemStack.STREAM_CODEC.decode(buf);
            boolean isUnlockedByDefault = buf.readBoolean();

            return new Questline(location, name, icon, isUnlockedByDefault);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf buf, Questline questline) {
            buf.writeResourceLocation(questline.getLocation());
            boolean hasName = questline.name != null;
            buf.writeBoolean(hasName);
            if (hasName) {
                buf.writeUtf(questline.name);
            }
            ItemStack.STREAM_CODEC.encode(buf, questline.getIcon());
            buf.writeBoolean(questline.isUnlockedByDefault());
        }
    }
}
