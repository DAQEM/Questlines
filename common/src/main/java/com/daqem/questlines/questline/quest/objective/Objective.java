package com.daqem.questlines.questline.quest.objective;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Objective implements IActionHolder {

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

    public static class Serializer implements ISerializer<Objective> {

        @Override
        public Objective fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return null;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, Objective type) {

        }

        @Override
        public Objective fromNBT(CompoundTag compoundTag) {
            return null;
        }

        @Override
        public CompoundTag toNBT(Objective type) {
            return null;
        }

        @Override
        public Objective deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return null;
        }

        @Override
        public JsonElement serialize(Objective objective, Type type, JsonSerializationContext jsonSerializationContext) {
            return null;
        }
    }
}
