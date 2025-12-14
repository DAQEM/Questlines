package com.daqem.questlines.questline.quest.objective;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.IActionSerializer;
import com.daqem.arc.api.action.holder.AbstractActionHolder;
import com.daqem.arc.api.action.holder.IActionHolderSerializer;
import com.daqem.arc.api.action.holder.IActionHolderType;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.serializer.ISerializable;
import com.daqem.questlines.data.serializer.ISerializer;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

public class Objective extends AbstractActionHolder {

    private final @Nullable String name;
    private final int goal;

    public Objective(ResourceLocation location, @Nullable String name, int goal) {
        super(location);
        this.name = name;
        this.goal = goal;
    }

    public int getGoal() {
        return goal;
    }

    @Override
    public IActionHolderType<?> getType() {
        return QuestlinesActionHolderType.OBJECTIVE;
    }

    public Component getName(ObjectiveProgress progress) {
        MutableComponent name = this.name != null ? Questlines.literal(this.name) : Questlines.translatable("objective." + location.toString().replace(":", ".").replace("/", ".") + ".name");
        return name.append(Questlines.translatable("objective.progress", progress.getProgress(), goal));
    }

    public static class Serializer implements IActionHolderSerializer<Objective>, JsonDeserializer<Objective> {

        @Override
        public Objective deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return fromJson(jsonObject, getResourceLocation(jsonObject, "location"));
        }

        @Override
        public Objective fromJson(JsonObject jsonObject, ResourceLocation location) {
            String id = jsonObject.get("id").getAsString();
            int goal = jsonObject.get("goal").getAsInt();

            return new Objective(
                    ResourceLocation.parse(location + "/" + id),
                    GsonHelper.getAsString(jsonObject, "name", null),
                    goal
            );
        }

        @Override
        public Objective fromNetwork(RegistryFriendlyByteBuf buf, ResourceLocation location) {
            boolean hasName = buf.readBoolean();
            String name = hasName ? buf.readUtf() : null;
            int goal = buf.readInt();
            return new Objective(location, name, goal);
        }

        public Objective fromNetwork(RegistryFriendlyByteBuf buf) {
            ResourceLocation location = buf.readResourceLocation();
            return fromNetwork(buf, location);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf buf, Objective objective) {
            boolean hasName = objective.name != null;
            buf.writeBoolean(hasName);
            if (hasName) {
                buf.writeUtf(objective.name);
            }
            buf.writeInt(objective.getGoal());
            IActionHolderSerializer.super.toNetwork(buf, objective);
        }
    }
}
