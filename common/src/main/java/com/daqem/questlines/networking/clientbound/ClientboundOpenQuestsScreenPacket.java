package com.daqem.questlines.networking.clientbound;

import com.daqem.questlines.networking.QuestlinesNetworking;
import com.daqem.questlines.questline.QuestlineProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientboundOpenQuestsScreenPacket implements CustomPacketPayload {

    private final List<QuestlineProgress> questlineProgresses;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenQuestsScreenPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundOpenQuestsScreenPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundOpenQuestsScreenPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundOpenQuestsScreenPacket packet) {
            buf.writeCollection(packet.questlineProgresses, (buf1, questlineProgress) ->
                    questlineProgress.getSerializer().toNetwork((RegistryFriendlyByteBuf) buf1, questlineProgress));
        }
    };

    public ClientboundOpenQuestsScreenPacket(List<QuestlineProgress> questlineProgresses) {
        this.questlineProgresses = questlineProgresses;

    }

    public ClientboundOpenQuestsScreenPacket(FriendlyByteBuf buf) {
        this.questlineProgresses = buf.readList(buf1 ->
                new QuestlineProgress.Serializer().fromNetwork((RegistryFriendlyByteBuf) buf1));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return QuestlinesNetworking.CLIENTBOUND_OPEN_QUESTS_SCREEN;
    }

    public List<QuestlineProgress> getQuestlineProgresses() {
        return questlineProgresses;
    }
}
