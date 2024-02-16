package com.daqem.questlines.integration.arc.reward;

import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.serializer.RewardSerializer;
import com.daqem.questlines.Questlines;

public interface QuestlinesRewardSerializer<T extends IReward> extends RewardSerializer<T> {

    IRewardSerializer<ObjectiveOccurrenceReward> OBJECTIVE_OCCURRENCE = RewardSerializer.register(Questlines.getId("objective_occurrence"), new ObjectiveOccurrenceReward.Serializer());

    static void init() {
    }
}
