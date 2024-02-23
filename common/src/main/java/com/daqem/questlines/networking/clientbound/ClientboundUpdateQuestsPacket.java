package com.daqem.questlines.networking.clientbound;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.networking.QuestlinesNetworking;
import com.daqem.questlines.questline.quest.Quest;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class ClientboundUpdateQuestsPacket extends BaseS2CMessage {

    private final List<Quest> quests;

    public ClientboundUpdateQuestsPacket(List<Quest> quests) {
        this.quests = quests;
    }

    public ClientboundUpdateQuestsPacket(FriendlyByteBuf friendlyByteBuf) {
        this.quests = friendlyByteBuf.readList(friendlyByteBuf1 -> new Quest.Serializer().fromNetwork(friendlyByteBuf1));
    }

    @Override
    public MessageType getType() {
        return QuestlinesNetworking.CLIENTBOUND_UPDATE_QUESTS;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(quests, (friendlyByteBuf1, quest) -> quest.getSerializer().toNetwork(friendlyByteBuf1, quest));
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        Questlines.getInstance().getQuestManager().replaceQuests(quests);
    }
}
