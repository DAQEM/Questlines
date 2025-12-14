package com.daqem.questlines.player;

import com.daqem.arc.data.ActionData;
import com.daqem.questlines.questline.Questline;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;

import java.util.List;
import java.util.Optional;

public interface QuestlinesServerPlayer extends QuestlinesPlayer {

    void questlines$addStartQuestlines(List<Questline> questlines);
    Optional<ObjectiveProgress> questlines$getObjectiveProgress(Objective objective);
    void questlines$addObjectiveProgress(Objective objective, int amount, ActionData actionData);

    void questlines$resetActionHolders();
    void questlines$removeActionHolders();
    void questlines$addActionHolders();

    boolean questlines$hasQuestline(Questline questline);
    void questlines$resetQuestlines();
}
