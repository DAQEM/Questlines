package com.daqem.questlines.networking.serverbound;

import com.daqem.questlines.networking.QuestlinesNetworking;
import com.daqem.questlines.networking.clientbound.ClientboundOpenQuestsScreenPacket;
import com.daqem.questlines.player.QuestlinesServerPlayer;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ServerboundOpenQuestsScreenPacket implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpenQuestsScreenPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ServerboundOpenQuestsScreenPacket decode(RegistryFriendlyByteBuf buf) {
            return new ServerboundOpenQuestsScreenPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerboundOpenQuestsScreenPacket packet) {
        }
    };

    public ServerboundOpenQuestsScreenPacket() {
    }

    public ServerboundOpenQuestsScreenPacket(FriendlyByteBuf buf) {
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return QuestlinesNetworking.SERVERBOUND_OPEN_QUESTS_SCREEN;
    }

    public static void handleServerSide(ServerboundOpenQuestsScreenPacket packet, NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer instanceof QuestlinesServerPlayer questlinesServerPlayer) {
                NetworkManager.sendToPlayer(serverPlayer, new ClientboundOpenQuestsScreenPacket(questlinesServerPlayer.questlines$getQuestlines()));
            }
        }
    }
}
