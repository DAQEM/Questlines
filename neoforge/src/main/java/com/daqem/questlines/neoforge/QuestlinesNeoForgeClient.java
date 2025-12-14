package com.daqem.questlines.neoforge;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.client.QuestlinesClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(value = Questlines.MOD_ID, dist = Dist.CLIENT)
public class QuestlinesNeoForgeClient {

    public QuestlinesNeoForgeClient(IEventBus modEventBus) {
        QuestlinesClient.init();
        modEventBus.addListener(this::registerKeyBindings);
    }

    private void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(QuestlinesClient.OPEN_QUEST_SCREEN);
    }
}
