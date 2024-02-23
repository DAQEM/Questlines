package com.daqem.questlines.player;

import com.daqem.questlines.questline.QuestlineProgress;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface QuestlinesPlayer {

    Player questlines1_20_1$asPlayer();

    List<QuestlineProgress> questlines1_20_1$getQuestlines();
}
