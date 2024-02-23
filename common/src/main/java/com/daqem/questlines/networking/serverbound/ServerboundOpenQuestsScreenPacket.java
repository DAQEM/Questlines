package com.daqem.questlines.networking.serverbound;

import com.daqem.questlines.networking.QuestlinesNetworking;
import com.daqem.questlines.networking.clientbound.ClientboundOpenQuestsScreenPacket;
import com.daqem.questlines.player.QuestlinesServerPlayer;
import com.daqem.questlines.questline.QuestlineProgress;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ServerboundOpenQuestsScreenPacket extends BaseC2SMessage {

    public ServerboundOpenQuestsScreenPacket() {
    }

    public ServerboundOpenQuestsScreenPacket(FriendlyByteBuf buf) {
    }

    @Override
    public MessageType getType() {
        return QuestlinesNetworking.SERVERBOUND_OPEN_QUESTS_SCREEN;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer instanceof QuestlinesServerPlayer questlinesServerPlayer) {
                new ClientboundOpenQuestsScreenPacket(questlinesServerPlayer.questlines1_20_1$getQuestlines()).sendTo(serverPlayer);
            }
        }
    }
}
