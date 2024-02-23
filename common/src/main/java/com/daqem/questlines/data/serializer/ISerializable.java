package com.daqem.questlines.data.serializer;

public interface ISerializable<T> {

    ISerializer<T> getSerializer();
}
