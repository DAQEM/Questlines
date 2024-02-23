package com.daqem.questlines.mixin;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.questlines.Questlines;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.daqem.questlines.player.QuestlinesServerPlayer;
import com.daqem.questlines.questline.Questline;
import com.daqem.questlines.questline.QuestlineProgress;
import com.daqem.questlines.questline.quest.Quest;
import com.daqem.questlines.questline.quest.QuestProgress;
import com.daqem.questlines.questline.quest.objective.Objective;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
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
    @Final
    public MinecraftServer server;

    @Shadow
    public abstract void tick();

    @Shadow
    public ServerGamePacketListenerImpl connection;
    @Unique
    private static final String QUESTLINES_TAG = "Questlines";

    @Unique
    private List<QuestlineProgress> questlines1_20_1$questlines = new ArrayList<>();

    public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Override
    public List<QuestlineProgress> questlines1_20_1$getQuestlines() {
        return questlines1_20_1$questlines;
    }

    @Override
    public void questlines1_20_1$addStartQuestlines(List<Questline> questlines) {
        questlines.stream()
                .filter(questline -> !questlines1_20_1$hasQuestline(questline))
                .forEach(questline -> questline.getStartQuest().ifPresent(quest -> {
                    List<ObjectiveProgress> objectiveProgresses = quest.getObjectives().stream()
                            .map(ObjectiveProgress::new)
                            .collect(Collectors.toList());

                    QuestProgress startQuestProgress = new QuestProgress(quest, objectiveProgresses);
                    QuestlineProgress questlineProgress = new QuestlineProgress(questline, startQuestProgress);

                    questlines1_20_1$questlines.add(questlineProgress);
                }));
        questlines1_20_1$resetActionHolders();
    }

    @Override
    public void questlines1_20_1$resetActionHolders() {
            questlines1_20_1$removeActionHolders();
            questlines1_20_1$addActionHolders();
    }

    @Override
    public void questlines1_20_1$removeActionHolders() {
        if (this instanceof ArcServerPlayer arcServerPlayer) {
            List<IActionHolder> actionHolders = arcServerPlayer.arc$getActionHolders()
                    .stream()
                    .filter(actionHolder -> actionHolder.getType().equals(QuestlinesActionHolderType.OBJECTIVE))
                    .toList();
            actionHolders.forEach(arcServerPlayer::arc$removeActionHolder);
        }
    }

    @Override
public void questlines1_20_1$addActionHolders() {
    if (this instanceof ArcServerPlayer arcServerPlayer) {
        questlines1_20_1$questlines.stream()
                .flatMap(questline -> questline.getAllQuestProgresses().stream())
                .flatMap(questProgress -> questProgress.getObjectives().stream())
                .filter(objectiveProgress -> !objectiveProgress.isCompleted())
                .map(ObjectiveProgress::getObjective)
                .forEach(arcServerPlayer::arc$addActionHolder);
    }
}

    @Override
    public boolean questlines1_20_1$hasQuestline(Questline questline) {
        return questlines1_20_1$questlines.stream()
                .anyMatch(questlineProgress -> questlineProgress.getQuestline().getLocation().equals(questline.getLocation()));
    }

    @Override
    public void questlines1_20_1$resetQuestlines() {
        questlines1_20_1$questlines.clear();
        questlines1_20_1$addStartQuestlines(Questlines.getInstance().getQuestlineManager().getStartQuestlines());
    }

    @Override
    public Optional<ObjectiveProgress> questlines1_20_1$getObjectiveProgress(Objective objective) {
        return questlines1_20_1$questlines.stream()
                .map(QuestlineProgress::getAllQuestProgresses)
                .flatMap(List::stream)
                .map(QuestProgress::getObjectives)
                .flatMap(List::stream)
                .filter(objectiveProgress -> objectiveProgress.getObjective().equals(objective))
                .findFirst();
    }

    @Override
    public void questlines1_20_1$addObjectiveProgress(Objective objective, int amount) {
        questlines1_20_1$getObjectiveProgress(objective).ifPresent(objectiveProgress -> {
            boolean hadCompleted = objectiveProgress.getProgress() == objective.getGoal();
            objectiveProgress.addProgress(amount);
            boolean hasCompleted = objectiveProgress.getProgress() == objective.getGoal();
            if (!hadCompleted && hasCompleted) {
                questlines1_20_1$broadcastCompletionMessage();
                QuestProgress questProgress = questlines1_20_1$findCompletedQuest();
                if (questProgress != null) {
                    questlines1_20_1$processCompletedQuest(questProgress);
                }
            }
        });
    }

    @Unique
    private void questlines1_20_1$broadcastCompletionMessage() {
        questlines1_20_1$getServer().ifPresent(server -> {
            server.getPlayerList().broadcastSystemMessage(
                    Questlines.literal("HIEP HIEP HOERA"), false
            );
        });
    }

    @Unique
    private QuestProgress questlines1_20_1$findCompletedQuest() {
        return questlines1_20_1$questlines.stream()
                .map(QuestlineProgress::getAllQuestProgresses)
                .flatMap(List::stream)
                .filter(questProgress1 -> questProgress1.getObjectives().stream()
                        .allMatch(ObjectiveProgress::isCompleted))
                .findFirst()
                .orElse(null);
    }

    @Unique
    private void questlines1_20_1$processCompletedQuest(QuestProgress questProgress) {
        //check if all objectives are completed
        boolean allCompleted = questProgress.getObjectives().stream()
                .allMatch(ObjectiveProgress::isCompleted);

        if (allCompleted) {
            QuestlineProgress questlineProgress = questlines1_20_1$questlines.stream()
                    .filter(questlineProgress1 -> questlineProgress1.getAllQuestProgresses().contains(questProgress))
                    .findFirst()
                    .orElse(null);

            if (questlineProgress != null) {
                List<Quest> nextQuests = questlineProgress.getQuestline().getAllQuests().stream()
                        .filter(quest -> quest.getParent().isPresent() && quest.getParent().get().equals(questProgress.getQuest()))
                        .toList();

                nextQuests.forEach(quest -> {
                    QuestProgress newQuestProgress = new QuestProgress(
                            quest,
                            quest.getObjectives().stream()
                                    .map(objective1 -> new ObjectiveProgress(objective1, 0))
                                    .toList()
                    );

                    questProgress.addChild(newQuestProgress);

                    if (newQuestProgress.isCompleted()) {
                        questlines1_20_1$processCompletedQuest(newQuestProgress);
                    }
                });
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V")
    private void restoreFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if (oldPlayer instanceof QuestlinesServerPlayer) {
            this.questlines1_20_1$questlines = ((QuestlinesServerPlayer) oldPlayer).questlines1_20_1$getQuestlines();
        }
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
    private void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        CompoundTag questlinesTag = new CompoundTag();
        for (QuestlineProgress questline : questlines1_20_1$questlines) {
            questlinesTag.put(
                    questline.getQuestline().getLocation().toString(),
                    questline.getSerializer().toNBT(questline)
            );
        }
        tag.put(QUESTLINES_TAG, questlinesTag);
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
    private void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(QUESTLINES_TAG)) {
            CompoundTag questlinesTag = tag.getCompound(QUESTLINES_TAG);
            for (String key : questlinesTag.getAllKeys()) {
                QuestlineProgress.Serializer serializer = new QuestlineProgress.Serializer();
                QuestlineProgress questline = serializer.fromNBT(questlinesTag.getCompound(key), new ResourceLocation(key));
                if (questline != null) {
                    questlines1_20_1$questlines.add(questline);
                } else {
                    Questlines.LOGGER.error("Failed to load questline progress for questline with location: " + key);
                }
            }
        }
        questlines1_20_1$resetActionHolders();
    }

    @Override
    public Player questlines1_20_1$asPlayer() {
        return this;
    }

    @Override
    public ServerPlayer questlines1_20_1$asServerPlayer() {
        return (ServerPlayer) (Object) this;
    }

    @Override
    public Optional<MinecraftServer> questlines1_20_1$getServer() {
        return Optional.ofNullable(this.getServer());
    }
}
