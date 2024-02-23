package com.daqem.questlines.networking.clientbound;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.networking.QuestlinesNetworking;
import com.daqem.questlines.questline.Questline;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class ClientboundUpdateQuestlinesPacket extends BaseS2CMessage {

    public List<Questline> questlines;

    public ClientboundUpdateQuestlinesPacket(List<Questline> questlines) {
        this.questlines = questlines;
    }

    public ClientboundUpdateQuestlinesPacket(FriendlyByteBuf friendlyByteBuf) {
        this.questlines = friendlyByteBuf.readList(friendlyByteBuf1 -> new Questline.Serializer().fromNetwork(friendlyByteBuf1));
    }

    @Override
    public MessageType getType() {
        return QuestlinesNetworking.CLIENTBOUND_UPDATE_QUESTLINES;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(questlines, (friendlyByteBuf1, questline) -> questline.getSerializer().toNetwork(friendlyByteBuf1, questline));
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        Questlines.getInstance().getQuestlineManager().replaceQuestlines(questlines);
    }
}
