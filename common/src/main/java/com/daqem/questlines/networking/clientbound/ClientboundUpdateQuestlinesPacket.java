package com.daqem.questlines.networking.clientbound;

import com.daqem.questlines.networking.QuestlinesNetworking;
import com.daqem.questlines.questline.Questline;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientboundUpdateQuestlinesPacket implements CustomPacketPayload {

    public List<Questline> questlines;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateQuestlinesPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundUpdateQuestlinesPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundUpdateQuestlinesPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundUpdateQuestlinesPacket packet) {
            buf.writeCollection(packet.questlines, (buf1, questline) ->
                    questline.getSerializer().toNetwork((RegistryFriendlyByteBuf) buf1, questline));
        }
    };

    public ClientboundUpdateQuestlinesPacket(List<Questline> questlines) {
        this.questlines = questlines;
    }

    public ClientboundUpdateQuestlinesPacket(FriendlyByteBuf buf) {
        this.questlines = buf.readList(buf1 ->
                new Questline.Serializer().fromNetwork((RegistryFriendlyByteBuf) buf1));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return QuestlinesNetworking.CLIENTBOUND_UPDATE_QUESTLINES;
    }

    public List<Questline> getQuestlines() {
        return questlines;
    }
}
