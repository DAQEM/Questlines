package com.daqem.questlines.integration.arc.reward;

import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.IRewardType;
import com.daqem.questlines.Questlines;

public interface QuestlinesRewardType<T extends IReward> extends IRewardType<T> {

    IRewardType<ObjectiveOccurrenceReward> OBJECTIVE_OCCURRENCE = IRewardType.register(Questlines.getId("objective_occurrence"), new ObjectiveOccurrenceReward.Serializer());

    static void init() {
    }
}
