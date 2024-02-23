package com.daqem.questlines.networking.clientbound;

import com.daqem.questlines.client.gui.QuestsScreen;
import com.daqem.questlines.networking.QuestlinesNetworking;
import com.daqem.questlines.questline.QuestlineProgress;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class ClientboundOpenQuestsScreenPacket extends BaseS2CMessage {

    private final List<QuestlineProgress> questlineProgresses;

    public ClientboundOpenQuestsScreenPacket(List<QuestlineProgress> questlineProgresses) {
        this.questlineProgresses = questlineProgresses;

    }

    public ClientboundOpenQuestsScreenPacket(FriendlyByteBuf buf) {
        this.questlineProgresses = buf.readList(buf1 ->
                new QuestlineProgress.Serializer().fromNetwork(buf1));
    }

    @Override
    public MessageType getType() {
        return QuestlinesNetworking.CLIENTBOUND_OPEN_QUESTS_SCREEN;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(questlineProgresses, (buf1, questlineProgress) ->
                questlineProgress.getSerializer().toNetwork(buf1, questlineProgress));
    }

    @Override
    @Environment(value= EnvType.CLIENT)
    public void handle(NetworkManager.PacketContext context) {
        Minecraft.getInstance().setScreen(new QuestsScreen(questlineProgresses));
    }
}
