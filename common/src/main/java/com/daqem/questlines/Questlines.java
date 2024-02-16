package com.daqem.questlines;

import com.daqem.questlines.config.QuestlinesConfig;
import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.QuestlineManager;
import com.daqem.questlines.integration.arc.action.holder.QuestlinesActionHolderType;
import com.daqem.questlines.integration.arc.reward.QuestlinesRewardSerializer;
import com.daqem.questlines.integration.arc.reward.QuestlinesRewardType;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public abstract class Questlines {

    public static final String MOD_ID = "questlines";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static Questlines instance = null;

    public Questlines() {
        if (instance != null) {
            throw new IllegalStateException("Questlines has already been initialized");
        }
        instance = this;

        QuestlinesConfig.init();
        QuestlinesActionHolderType.init();
        QuestlinesRewardSerializer.init();
        QuestlinesRewardType.init();
    }

    public static void init() {
    }

    public static Questlines getInstance() {
        return instance;
    }

    public static ResourceLocation getId(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    abstract public QuestlineManager getQuestlineManager();
    abstract public QuestManager getQuestManager();

}
