package com.daqem.questlines.questline.quest.objective;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Objective implements IActionHolder, ISerializable<Objective> {

    private final Map<ResourceLocation, IAction> actions = new HashMap<>();

    private final ResourceLocation location;
    private final @Nullable String name;
    private final int goal;

    public Objective(ResourceLocation location, @Nullable String name, int goal) {
        this.location = location;
        this.name = name;
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

    public Component getName(ObjectiveProgress progress) {
        MutableComponent name = this.name != null ? Questlines.literal(this.name) : Questlines.translatable("objective." + location.toString().replace(":", ".").replace("/", ".") + ".name");
        return name.append(Questlines.translatable("objective.progress", progress.getProgress(), goal));
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
                    GsonHelper.getAsString(jsonObject, "name", null),
                    goal
            );
        }

        @Override
        public Objective fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            ResourceLocation location = friendlyByteBuf.readResourceLocation();

            boolean hasName = friendlyByteBuf.readBoolean();
            String name = hasName ? friendlyByteBuf.readUtf() : null;

            int goal = friendlyByteBuf.readInt();
            List<IAction> actions = friendlyByteBuf.readList(IActionSerializer::fromNetwork);

            Objective objective = new Objective(location, name, goal);

            for (IAction action : actions) {
                objective.addAction(action);
            }

            return objective;
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, Objective type) {
            friendlyByteBuf.writeResourceLocation(type.getLocation());
            boolean hasName = type.name != null;
            friendlyByteBuf.writeBoolean(hasName);
            if (hasName) {
                friendlyByteBuf.writeUtf(type.name);
            }
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
