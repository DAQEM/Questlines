package com.daqem.questlines.client.networking;

import com.daqem.questlines.data.QuestlineManager;
import com.daqem.questlines.networking.clientbound.ClientboundUpdateQuestlinesPacket;
import dev.architectury.networking.NetworkManager;

public class ClientboundUpdateQuestlinesPacketHandler {

    public static void handleClientSide(ClientboundUpdateQuestlinesPacket packet, NetworkManager.PacketContext context) {
        QuestlineManager.getInstance().setQuestlines(packet.getQuestlines());
    }
}
