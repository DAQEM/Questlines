package com.daqem.questlines.questline.quest.objective;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
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

    public int getGoal() {
        return goal;
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
            ResourceLocation location = friendlyByteBuf.readResourceLocation();
            int goal = friendlyByteBuf.readInt();
            List<IAction> actions = friendlyByteBuf.readList(IActionSerializer::fromNetwork);

            Objective objective = new Objective(location, goal);

            for (IAction action : actions) {
                objective.addAction(action);
            }

            return objective;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, Objective type) {
            friendlyByteBuf.writeResourceLocation(type.getLocation());
            friendlyByteBuf.writeInt(type.getGoal());
            friendlyByteBuf.writeCollection(type.getActions(),
                    (friendlyByteBuf1, action) -> IActionSerializer.toNetwork(action, friendlyByteBuf1));
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
