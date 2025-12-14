package com.daqem.questlines.event;

import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.QuestlineManager;
import com.daqem.questlines.networking.clientbound.ClientboundUpdateQuestlinesPacket;
import com.daqem.questlines.networking.clientbound.ClientboundUpdateQuestsPacket;
import com.daqem.questlines.player.QuestlinesServerPlayer;
import com.daqem.questlines.questline.Questline;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.dedicated.DedicatedServer;

import java.util.List;

public class PlayerJoinEvent {

    public static void registerEvent() {
        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player.level().getServer() instanceof DedicatedServer) {
                NetworkManager.sendToPlayer(player, new ClientboundUpdateQuestsPacket(QuestManager.getInstance().getQuests()));
                NetworkManager.sendToPlayer(player, new ClientboundUpdateQuestlinesPacket(QuestlineManager.getInstance().getQuestlines()));
            }

            if (player instanceof QuestlinesServerPlayer serverPlayer) {
                List<Questline> questlines = QuestlineManager.getInstance().getStartQuestlines();
                serverPlayer.questlines$addStartQuestlines(questlines);
            }
        });
    }
}
