package com.daqem.questlines.event;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.networking.clientbound.ClientboundUpdateQuestlinesPacket;
import com.daqem.questlines.networking.clientbound.ClientboundUpdateQuestsPacket;
import com.daqem.questlines.player.QuestlinesServerPlayer;
import com.daqem.questlines.questline.Questline;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.dedicated.DedicatedServer;

import java.util.List;

public class PlayerJoinEvent {

    public static void registerEvent() {
        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player.server instanceof DedicatedServer server) {
                new ClientboundUpdateQuestsPacket(Questlines.getInstance().getQuestManager().getQuests()).sendTo(player);
                new ClientboundUpdateQuestlinesPacket(Questlines.getInstance().getQuestlineManager().getQuestlines()).sendTo(player);
            }

            if (player instanceof QuestlinesServerPlayer serverPlayer) {
                List<Questline> questlines = Questlines.getInstance().getQuestlineManager().getStartQuestlines();
                serverPlayer.questlines1_20_1$addStartQuestlines(questlines);
            }
        });
    }
}
