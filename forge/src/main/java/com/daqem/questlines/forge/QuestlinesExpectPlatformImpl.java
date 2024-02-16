package com.daqem.questlines.forge;

import com.daqem.questlines.QuestlinesExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class QuestlinesExpectPlatformImpl {
    /**
     * This is our actual method to {@link QuestlinesExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
