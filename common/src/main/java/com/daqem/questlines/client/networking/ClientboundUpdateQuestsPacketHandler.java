package com.daqem.questlines.client.networking;

import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.networking.clientbound.ClientboundUpdateQuestsPacket;
import dev.architectury.networking.NetworkManager;

public class ClientboundUpdateQuestsPacketHandler {

    public static void handleClientSide(ClientboundUpdateQuestsPacket packet, NetworkManager.PacketContext context) {
        QuestManager.getInstance().setQuests(packet.getQuests());
    }
}
