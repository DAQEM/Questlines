package com.daqem.questlines.data.serializer;

import com.daqem.arc.data.serializer.ArcSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface ISerializer<T> extends JsonSerializer<T>, JsonDeserializer<T>, ArcSerializer {

    T fromNetwork(FriendlyByteBuf friendlyByteBuf);

    void toNetwork(FriendlyByteBuf friendlyByteBuf, T type);

    T fromNBT(CompoundTag compoundTag);

    CompoundTag toNBT(T type);
}
