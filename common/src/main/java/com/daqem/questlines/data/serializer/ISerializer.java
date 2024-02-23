package com.daqem.questlines.data.serializer;

import com.daqem.arc.data.serializer.ArcSerializer;
import com.google.gson.JsonDeserializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface ISerializer<T> extends JsonDeserializer<T>, ArcSerializer {

    T fromNetwork(FriendlyByteBuf friendlyByteBuf);

    void toNetwork(FriendlyByteBuf friendlyByteBuf, T type);

    T fromNBT(CompoundTag compoundTag, ResourceLocation location);

    CompoundTag toNBT(T type);
}
