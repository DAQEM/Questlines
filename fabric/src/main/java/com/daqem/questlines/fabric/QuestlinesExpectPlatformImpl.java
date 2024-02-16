package com.daqem.questlines.fabric;

import com.daqem.questlines.QuestlinesExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class QuestlinesExpectPlatformImpl {
    /**
     * This is our actual method to {@link QuestlinesExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
