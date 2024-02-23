package com.daqem.questlines.integration.arc.reward;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.serializer.RewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.questlines.player.QuestlinesServerPlayer;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public class ObjectiveOccurrenceReward extends AbstractReward {

    private final int amount;

    public ObjectiveOccurrenceReward(double chance, int priority, int amount) {
        super(chance, priority);
        this.amount = amount;
    }

    @Override
    public IRewardType<?> getType() {
        return QuestlinesRewardType.OBJECTIVE_OCCURRENCE;
    }

    @Override
    public IRewardSerializer<? extends IReward> getSerializer() {
        return QuestlinesRewardSerializer.OBJECTIVE_OCCURRENCE;
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        IActionHolder source = actionData.getSourceActionHolder();
        if (source instanceof Objective objective) {
            ArcPlayer player = actionData.getPlayer();
            if (player instanceof QuestlinesServerPlayer serverPlayer) {
                serverPlayer.questlines1_20_1$addObjectiveProgress(objective, amount);
            }
        }
        return new ActionResult();
    }

    public static class Serializer implements RewardSerializer<ObjectiveOccurrenceReward> {

        @Override
        public ObjectiveOccurrenceReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new ObjectiveOccurrenceReward(chance, priority,
                    GsonHelper.getAsInt(jsonObject, "amount", 1)
            );
        }

        @Override
        public ObjectiveOccurrenceReward fromNetwork(FriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new ObjectiveOccurrenceReward(chance, priority,
                    friendlyByteBuf.readInt()
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, ObjectiveOccurrenceReward type) {
            RewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.amount);
        }
    }
}
