package com.daqem.questlines.event;

import com.daqem.questlines.command.QuestlinesCommand;
import dev.architectury.event.events.common.CommandRegistrationEvent;

public class RegisterCommandEvent {

    public static void registerEvent() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            QuestlinesCommand.registerCommand(dispatcher);
        });
    }
}
