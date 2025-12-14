package com.daqem.questlines.mixin;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.data.ActionData;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.QuestlineManager;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.daqem.questlines.player.QuestlinesServerPlayer;
import com.daqem.questlines.player.ServerPlayerData;
import com.daqem.questlines.questline.Questline;
import com.daqem.questlines.questline.QuestlineProgress;
import com.daqem.questlines.questline.quest.Quest;
import com.daqem.questlines.questline.quest.QuestProgress;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements QuestlinesServerPlayer {

    @Shadow
    public abstract void sendSystemMessage(Component arg, boolean bl);

    @Unique
    private List<QuestlineProgress> questlines$questlines = new ArrayList<>();

    public MixinServerPlayer(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public List<QuestlineProgress> questlines$getQuestlines() {
        return questlines$questlines;
    }

    @Override
    public void questlines$addStartQuestlines(List<Questline> questlines) {
        questlines.stream()
                .filter(questline -> !questlines$hasQuestline(questline))
                .forEach(questline -> questline.getStartQuest().ifPresent(quest -> {
                    List<ObjectiveProgress> objectiveProgresses = quest.getObjectives().stream()
                            .map(ObjectiveProgress::new)
                            .collect(Collectors.toList());

                    QuestProgress startQuestProgress = new QuestProgress(quest, objectiveProgresses);
                    QuestlineProgress questlineProgress = new QuestlineProgress(questline, startQuestProgress);

                    questlines$questlines.add(questlineProgress);
                }));
        questlines$resetActionHolders();
    }

    @Override
    public void questlines$resetActionHolders() {
        questlines$removeActionHolders();
        questlines$addActionHolders();
    }

    @Override
    public void questlines$removeActionHolders() {
        if (this instanceof ArcServerPlayer arcServerPlayer) {
            List<IActionHolder> actionHolders = arcServerPlayer.arc$getActionHolders()
                    .stream()
                    .filter(actionHolder -> actionHolder.getType().equals(QuestlinesActionHolderType.OBJECTIVE))
                    .toList();
            actionHolders.forEach(arcServerPlayer::arc$removeActionHolder);
        }
    }

    @Override
    public void questlines$addActionHolders() {
        if (this instanceof ArcServerPlayer arcServerPlayer) {
            questlines$questlines.stream()
                    .flatMap(questline -> questline.getAllQuestProgresses().stream())
                    .flatMap(questProgress -> questProgress.getObjectives().stream())
                    .filter(objectiveProgress -> !objectiveProgress.isCompleted())
                    .map(ObjectiveProgress::getObjective)
                    .forEach(arcServerPlayer::arc$addActionHolder);
        }
    }

    @Override
    public boolean questlines$hasQuestline(Questline questline) {
        return questlines$questlines.stream()
                .anyMatch(questlineProgress -> questlineProgress.getQuestline().getLocation().equals(questline.getLocation()));
    }

    @Override
    public void questlines$resetQuestlines() {
        questlines$questlines.clear();
        questlines$addStartQuestlines(QuestlineManager.getInstance().getStartQuestlines());
    }

    @Override
    public Optional<ObjectiveProgress> questlines$getObjectiveProgress(Objective objective) {
        return questlines$questlines.stream()
                .map(QuestlineProgress::getAllQuestProgresses)
                .flatMap(List::stream)
                .map(QuestProgress::getObjectives)
                .flatMap(List::stream)
                .filter(objectiveProgress -> objectiveProgress.getObjective().equals(objective))
                .findFirst();
    }

    @Override
    public void questlines$addObjectiveProgress(Objective objective, int amount, ActionData actionData) {
        questlines$getObjectiveProgress(objective).ifPresent(objectiveProgress -> {
            boolean hadCompleted = objectiveProgress.getProgress() == objective.getGoal();
            objectiveProgress.addProgress(amount);
            boolean hasCompleted = objectiveProgress.getProgress() == objective.getGoal();
            if (!hadCompleted && hasCompleted) {
                questlines$broadcastCompletionMessage(objectiveProgress);
                questlines$findCompletedQuest(objectiveProgress).ifPresent(progress ->
                        questlines$processCompletedQuest(progress, actionData));
            }
            questlines$resetActionHolders();
        });
    }

    @Unique
    private void questlines$broadcastCompletionMessage(ObjectiveProgress objectiveProgress) {
        sendSystemMessage(
                Questlines.literal("You completed the objective: " + objectiveProgress.getObjective().getName(objectiveProgress).getString()), false
        );
    }

    @Unique
    private Optional<QuestProgress> questlines$findCompletedQuest(ObjectiveProgress objectiveProgress) {
        return questlines$questlines.stream()
                .flatMap(questlineProgress -> questlineProgress.getAllQuestProgresses().stream())
                .filter(questProgress1 -> questProgress1.getObjectives().contains(objectiveProgress))
                .findFirst();
    }

    @Unique
    private void questlines$processCompletedQuest(QuestProgress questProgress, ActionData actionData) {
        if (!questProgress.isCompleted()) {
            return;
        }

        questProgress.getQuest().getRewards().forEach(reward -> reward.apply(actionData));

        QuestlineProgress.findQuestlineProgress(questlines$questlines, questProgress)
                .ifPresent(questlineProgress -> {
                    List<Quest> quests = QuestlineProgress.findQuestsForParent(questlineProgress, questProgress);
                    quests.forEach(quest -> {
                        QuestProgress newQuestProgress = quest.createQuestProgress();
                        questProgress.addChild(newQuestProgress);
                        if (newQuestProgress.isCompleted()) {
                            questlines$processCompletedQuest(newQuestProgress, actionData);
                        }
                    });
                });
    }

    @Inject(at = @At("TAIL"), method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V")
    private void restoreFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if (oldPlayer instanceof QuestlinesServerPlayer) {
            this.questlines$questlines = ((QuestlinesServerPlayer) oldPlayer).questlines$getQuestlines();
        }
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
    private void addAdditionalSaveData(ValueOutput valueOutput, CallbackInfo ci) {
        valueOutput.store("Questlines", ServerPlayerData.CODEC, new ServerPlayerData(
                questlines$questlines
        ));
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
    private void readAdditionalSaveData(ValueInput valueInput, CallbackInfo ci) {
        valueInput.read("Questlines", ServerPlayerData.CODEC).ifPresent(data -> {
            this.questlines$questlines = data.questlines().stream()
                    .filter(q -> q.getQuestline() != null)
                    .collect(Collectors.toCollection(ArrayList::new));
            questlines$resetActionHolders();
        });
    }
}
