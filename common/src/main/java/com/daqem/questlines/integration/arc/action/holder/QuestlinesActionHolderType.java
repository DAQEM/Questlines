package com.daqem.questlines.integration.arc.action.holder;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.type.ActionHolderType;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.questline.quest.objective.Objective;

public interface QuestlinesActionHolderType<T extends IActionHolder> extends ActionHolderType<T> {

    ActionHolderType<Objective> OBJECTIVE = ActionHolderType.register(Questlines.getId("objective"));

    static void init() {
    }
}
