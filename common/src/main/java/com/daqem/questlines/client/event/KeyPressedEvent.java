package com.daqem.questlines.client.event;

import com.daqem.questlines.client.QuestlinesClient;
import com.daqem.questlines.client.gui.QuestsScreen;
import com.daqem.questlines.networking.serverbound.ServerboundOpenQuestsScreenPacket;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.gui.screens.Screen;

public class KeyPressedEvent {

    public static void registerEvent() {
        ClientRawInputEvent.KEY_PRESSED.register((client, keyCode, scanCode, action, modifiers) -> {
            Screen screen = client.screen;
            if (QuestlinesClient.OPEN_QUEST_SCREEN.matches(keyCode, scanCode) && action == 1) {
                if (screen instanceof QuestsScreen) screen.onClose();
                else if (screen == null) new ServerboundOpenQuestsScreenPacket().sendToServer();
            }
            return EventResult.pass();
        });
    }
}
