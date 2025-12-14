package com.daqem.questlines.player;

import com.daqem.questlines.questline.QuestlineProgress;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record ServerPlayerData(List<QuestlineProgress> questlines) {
    public static final Codec<ServerPlayerData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    QuestlineProgress.CODEC.listOf().fieldOf("questlines").forGetter(ServerPlayerData::questlines)
            ).apply(instance, ServerPlayerData::new)
    );
}
