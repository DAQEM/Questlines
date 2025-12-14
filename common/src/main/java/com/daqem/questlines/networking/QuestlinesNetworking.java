package com.daqem.questlines.networking;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.client.networking.ClientboundOpenQuestsScreenPacketHandler;
import com.daqem.questlines.client.networking.ClientboundUpdateQuestlinesPacketHandler;
import com.daqem.questlines.client.networking.ClientboundUpdateQuestsPacketHandler;
import com.daqem.questlines.networking.clientbound.ClientboundOpenQuestsScreenPacket;
import com.daqem.questlines.networking.clientbound.ClientboundUpdateQuestlinesPacket;
import com.daqem.questlines.networking.clientbound.ClientboundUpdateQuestsPacket;
import com.daqem.questlines.networking.serverbound.ServerboundOpenQuestsScreenPacket;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface QuestlinesNetworking {

    CustomPacketPayload.Type<ServerboundOpenQuestsScreenPacket> SERVERBOUND_OPEN_QUESTS_SCREEN = new CustomPacketPayload.Type<>(Questlines.getId("serverbound_open_quests_screen"));

    CustomPacketPayload.Type<ClientboundOpenQuestsScreenPacket> CLIENTBOUND_OPEN_QUESTS_SCREEN = new CustomPacketPayload.Type<>(Questlines.getId("clientbound_open_quests_screen"));
    CustomPacketPayload.Type<ClientboundUpdateQuestsPacket> CLIENTBOUND_UPDATE_QUESTS = new CustomPacketPayload.Type<>(Questlines.getId("clientbound_update_quests"));
    CustomPacketPayload.Type<ClientboundUpdateQuestlinesPacket> CLIENTBOUND_UPDATE_QUESTLINES = new CustomPacketPayload.Type<>(Questlines.getId("clientbound_update_questlines"));

    static void initClient() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_OPEN_QUESTS_SCREEN, ClientboundOpenQuestsScreenPacket.STREAM_CODEC, ClientboundOpenQuestsScreenPacketHandler::handleClientSide);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_UPDATE_QUESTS, ClientboundUpdateQuestsPacket.STREAM_CODEC, ClientboundUpdateQuestsPacketHandler::handleClientSide);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_UPDATE_QUESTLINES, ClientboundUpdateQuestlinesPacket.STREAM_CODEC, ClientboundUpdateQuestlinesPacketHandler::handleClientSide);
    }

    static void initCommon() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_OPEN_QUESTS_SCREEN, ServerboundOpenQuestsScreenPacket.STREAM_CODEC, ServerboundOpenQuestsScreenPacket::handleServerSide);
    }

    static void initServer() {
        NetworkManager.registerS2CPayloadType(CLIENTBOUND_OPEN_QUESTS_SCREEN, ClientboundOpenQuestsScreenPacket.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(CLIENTBOUND_UPDATE_QUESTS, ClientboundUpdateQuestsPacket.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(CLIENTBOUND_UPDATE_QUESTLINES, ClientboundUpdateQuestlinesPacket.STREAM_CODEC);
    }

    static void init() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> QuestlinesNetworking::initClient);
        EnvExecutor.runInEnv(Env.SERVER, () -> QuestlinesNetworking::initServer);
        initCommon();
    }
}
