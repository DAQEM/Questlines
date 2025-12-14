package com.daqem.questlines.integration.arc.action.holder;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.IActionHolderType;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.questline.quest.objective.Objective;

public interface QuestlinesActionHolderType<T extends IActionHolder> extends IActionHolderType<T> {

    IActionHolderType<Objective> OBJECTIVE = IActionHolderType.register(Questlines.getId("objective"), new Objective.Serializer());

    static void init() {
    }
}
