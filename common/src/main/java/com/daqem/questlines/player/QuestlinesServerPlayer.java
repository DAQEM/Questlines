package com.daqem.questlines.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.IActionData;
import com.daqem.questlines.questline.Questline;
import com.daqem.questlines.questline.QuestlineProgress;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

public interface QuestlinesServerPlayer extends QuestlinesPlayer {

    ServerPlayer questlines1_20_1$asServerPlayer();
    Optional<MinecraftServer> questlines1_20_1$getServer();

    void questlines1_20_1$addStartQuestlines(List<Questline> questlines);
    Optional<ObjectiveProgress> questlines1_20_1$getObjectiveProgress(Objective objective);
    void questlines1_20_1$addObjectiveProgress(Objective objective, int amount, ActionData actionData);

    void questlines1_20_1$resetActionHolders();
    void questlines1_20_1$removeActionHolders();
    void questlines1_20_1$addActionHolders();

    boolean questlines1_20_1$hasQuestline(Questline questline);
    void questlines1_20_1$resetQuestlines();
}
