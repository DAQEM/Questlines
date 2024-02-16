package com.daqem.questlines.config;

import com.daqem.questlines.Questlines;
import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

public class QuestlinesConfig {

    public static void init() {
    }

    public static final Supplier<Boolean> isDebug;

    static {
        IConfigBuilder config = ConfigBuilders.newTomlConfig(Questlines.MOD_ID,null, false);

        config.push("debug");
        isDebug = config.comment("Enable debug mode").define("isDebug", false);
        config.pop();

        config.build();
    }
}
