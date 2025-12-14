package com.daqem.questlines.client.networking;

import com.daqem.questlines.client.gui.QuestsScreen;
import com.daqem.questlines.networking.clientbound.ClientboundOpenQuestsScreenPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

public class ClientboundOpenQuestsScreenPacketHandler {

    public static void handleClientSide(ClientboundOpenQuestsScreenPacket packet, NetworkManager.PacketContext context) {
        Minecraft.getInstance().setScreen(new QuestsScreen(packet.getQuestlineProgresses()));
    }
}
