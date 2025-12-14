package com.daqem.questlines.forge;

import com.daqem.questlines.data.QuestManager;
import com.daqem.questlines.data.QuestlineManager;
import com.daqem.questlines.forge.data.QuestManagerForge;
import com.daqem.questlines.forge.data.QuestlineManagerForge;
import dev.architectury.platform.forge.EventBuses;
import com.daqem.questlines.Questlines;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Questlines.MOD_ID)
public class QuestlinesForge extends Questlines {

    private static final QuestlineManagerForge QUESTLINE_MANAGER = new QuestlineManagerForge();
    private static final QuestManagerForge QUEST_MANAGER = new QuestManagerForge();

    public QuestlinesForge() {
        EventBuses.registerModEventBus(Questlines.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        init();

        DistExecutor.safeRunForDist(
                () -> SideProxyForge.Client::new,
                () -> SideProxyForge.Server::new
        );
    }

    @Override
    public QuestlineManager getQuestlineManager() {
        return QUESTLINE_MANAGER;
    }

    @Override
    public QuestManager getQuestManager() {
        return QUEST_MANAGER;
    }
}
