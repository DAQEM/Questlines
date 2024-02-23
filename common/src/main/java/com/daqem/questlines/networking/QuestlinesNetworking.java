package com.daqem.questlines.networking;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.networking.clientbound.ClientboundOpenQuestsScreenPacket;
import com.daqem.questlines.networking.clientbound.ClientboundUpdateQuestlinesPacket;
import com.daqem.questlines.networking.clientbound.ClientboundUpdateQuestsPacket;
import com.daqem.questlines.networking.serverbound.ServerboundOpenQuestsScreenPacket;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;

public interface QuestlinesNetworking {

    SimpleNetworkManager NETWORK_MANAGER = SimpleNetworkManager.create(Questlines.MOD_ID);

    MessageType SERVERBOUND_OPEN_QUESTS_SCREEN = NETWORK_MANAGER.registerC2S("serverbound_open_quests_screen", ServerboundOpenQuestsScreenPacket::new);

    MessageType CLIENTBOUND_OPEN_QUESTS_SCREEN = NETWORK_MANAGER.registerS2C("clientbound_open_quests_screen", ClientboundOpenQuestsScreenPacket::new);
    MessageType CLIENTBOUND_UPDATE_QUESTS = NETWORK_MANAGER.registerS2C("clientbound_update_quests", ClientboundUpdateQuestsPacket::new);
    MessageType CLIENTBOUND_UPDATE_QUESTLINES = NETWORK_MANAGER.registerS2C("clientbound_update_questlines", ClientboundUpdateQuestlinesPacket::new);


    static void init() {
    }
}
