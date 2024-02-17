package com.daqem.questlines.event;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.player.QuestlinesServerPlayer;
import com.daqem.questlines.questline.Questline;
import dev.architectury.event.events.common.PlayerEvent;

import java.util.List;

public class PlayerJoinEvent {

    public static void registerEvent() {
        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player instanceof QuestlinesServerPlayer serverPlayer) {
                List<Questline> questlines = Questlines.getInstance().getQuestlineManager().getStartQuestlines();
                serverPlayer.questlines1_20_1$addStartQuestlines(questlines);
            }
        });
    }
}
