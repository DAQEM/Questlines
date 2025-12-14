package com.daqem.questlines.data.serializer;

import com.daqem.arc.data.serializer.ArcSerializer;
import com.google.gson.JsonDeserializer;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface ISerializer<T> extends JsonDeserializer<T>, ArcSerializer {

    T fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf);

    void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, T type);
}
