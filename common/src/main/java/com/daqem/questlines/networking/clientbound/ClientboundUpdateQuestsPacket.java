package com.daqem.questlines.networking.clientbound;

import com.daqem.questlines.networking.QuestlinesNetworking;
import com.daqem.questlines.questline.quest.Quest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientboundUpdateQuestsPacket implements CustomPacketPayload {

    private final List<Quest> quests;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateQuestsPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundUpdateQuestsPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundUpdateQuestsPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundUpdateQuestsPacket packet) {
            buf.writeCollection(packet.quests, (buf1, quest) ->
                    quest.getSerializer().toNetwork((RegistryFriendlyByteBuf) buf1, quest));

        }
    };

    public ClientboundUpdateQuestsPacket(List<Quest> quests) {
        this.quests = quests;
    }

    public ClientboundUpdateQuestsPacket(FriendlyByteBuf buf) {
        this.quests = buf.readList(buf1 ->
                new Quest.Serializer().fromNetwork((RegistryFriendlyByteBuf) buf1));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return QuestlinesNetworking.CLIENTBOUND_UPDATE_QUESTS;
    }

    public List<Quest> getQuests() {
        return quests;
    }
}
