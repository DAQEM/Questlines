package com.daqem.questlines.questline.quest.objective;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Objective implements IActionHolder, ISerializable<Objective> {

    private final Map<ResourceLocation, IAction> actions = new HashMap<>();

    private final ResourceLocation location;
    private final int goal;

    public Objective(ResourceLocation location, int goal) {
        this.location = location;
        this.goal = goal;
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public List<IAction> getActions() {
        return actions.values().stream().toList();
    }

    @Override
    public void addAction(IAction action) {
        actions.put(action.getLocation(), action);
    }

    @Override
    public IActionHolderType<?> getType() {
        return QuestlinesActionHolderType.OBJECTIVE;
    }

    @Override
    public ISerializer<Objective> getSerializer() {
        return new Serializer();
    }

    public static class Serializer implements ISerializer<Objective> {

        @Override
        public Objective deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String location = jsonObject.get("location").getAsString();
            String id = jsonObject.get("id").getAsString();
            int goal = jsonObject.get("goal").getAsInt();

            return new Objective(
                    new ResourceLocation(location + "/" + id),
                    goal
            );
        }

        @Override
        public Objective fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return null;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, Objective type) {

        }

        @Override
        public Objective fromNBT(CompoundTag compoundTag, ResourceLocation location) {
            return null;
        }

        @Override
        public CompoundTag toNBT(Objective type) {
            return null;
        }
    }
}
