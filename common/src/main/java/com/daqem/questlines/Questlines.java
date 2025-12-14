package com.daqem.questlines;

import com.daqem.questlines.config.QuestlinesConfig;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.QuestlineManager;
import com.daqem.questlines.event.PlayerJoinEvent;
import com.daqem.questlines.event.RegisterCommandEvent;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.daqem.questlines.integration.arc.reward.QuestlinesRewardType;
import com.daqem.questlines.networking.QuestlinesNetworking;
import com.mojang.logging.LogUtils;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class Questlines {

    public static final String MOD_ID = "questlines";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        QuestlinesConfig.init();
        QuestlinesNetworking.init();

        QuestlinesActionHolderType.init();
        QuestlinesRewardType.init();

        registerEvents();

        ReloadListenerRegistry.register(PackType.SERVER_DATA, new QuestlineManager(), getId("questlines"));
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new QuestManager(), getId("quests"));
    }

    private static void registerEvents() {
        PlayerJoinEvent.registerEvent();
        RegisterCommandEvent.registerEvent();
    }

    public static ResourceLocation getId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static MutableComponent literal(String str) {
        return Component.literal(str);
    }

    public static MutableComponent translatable(String str) {
        return translatable(str, TranslatableContents.NO_ARGS);
    }

    public static MutableComponent translatable(String str, Object... args) {
        return Component.translatable(MOD_ID + "." + str, args);
    }

    public static MutableComponent prefixedTranslatable(String str) {
        return prefixedTranslatable(str, TranslatableContents.NO_ARGS);
    }

    public static MutableComponent prefixedTranslatable(String str, Object... args) {
        return getChatPrefix().append(Component.translatable(MOD_ID + "." + str, args));
    }

    public static MutableComponent getChatPrefix() {
        return Component.empty().append(
                literal("[").withStyle(Style.EMPTY.withColor(QuestlinesConfig.secondaryColor.get()))
        ).append(
                translatable("name").withStyle(Style.EMPTY.withColor(QuestlinesConfig.primaryColor.get()))
        ).append(
                literal("] ").withStyle(Style.EMPTY.withColor(QuestlinesConfig.secondaryColor.get()))
        );
    }
}
