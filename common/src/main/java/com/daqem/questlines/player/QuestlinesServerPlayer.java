package com.daqem.questlines.player;

import com.daqem.questlines.questline.Questline;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface QuestlinesServerPlayer extends QuestlinesPlayer {

    ServerPlayer questlines1_20_1$asServerPlayer();

    void questlines1_20_1$addStartQuestlines(List<Questline> questlines);
}
