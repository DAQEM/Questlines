package com.daqem.questlines.mixin;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.data.QuestlineManager;
import com.daqem.questlines.player.QuestlinesServerPlayer;
import com.daqem.questlines.questline.Questline;
import com.daqem.questlines.questline.QuestlineProgress;
import com.daqem.questlines.questline.quest.Quest;
import com.daqem.questlines.questline.quest.QuestProgress;
import com.daqem.questlines.questline.quest.objective.ObjectiveProgress;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements QuestlinesServerPlayer {

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
        for (Questline questline : questlines) {
            if (questlines1_20_1$questlines.stream()
                    .noneMatch(questlineProgress -> questlineProgress.getQuestline().getLocation().equals(questline.getLocation()))) {
                Quest startQuest = questline.getStartQuest();
                questlines1_20_1$questlines.add(
                        startQuest == null ?
                                null :
                                new QuestlineProgress(
                                        questline,
                                        new QuestProgress(
                                                startQuest,
                                                startQuest.getObjectives().stream()
                                                        .map(objective -> new ObjectiveProgress(objective, 0))
                                                        .toList()
                                        )));
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
    }

    @Override
    public Player questlines1_20_1$asPlayer() {
        return this;
    }

    @Override
    public ServerPlayer questlines1_20_1$asServerPlayer() {
        return (ServerPlayer) (Object) this;
    }
}
