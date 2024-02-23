package com.daqem.questlines.integration.arc.reward;

import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.daqem.questlines.Questlines;

public interface QuestlinesRewardType<T extends IReward> extends RewardType<T> {

    IRewardType<ObjectiveOccurrenceReward> OBJECTIVE_OCCURRENCE = RewardType.register(Questlines.getId("objective_occurrence"));

    static void init() {
    }
}
