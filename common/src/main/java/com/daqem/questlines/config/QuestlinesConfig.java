package com.daqem.questlines.config;

import com.daqem.questlines.Questlines;
import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

public class QuestlinesConfig {

    public static void init() {
    }

    public static final Supplier<Integer> primaryColor;
    public static final Supplier<Integer> secondaryColor;

    public static final Supplier<Boolean> isDebug;

    static {
        IConfigBuilder config = ConfigBuilders.newTomlConfig(Questlines.MOD_ID,null, false);

        config.push("colors");
        primaryColor = config.comment("The primary color used for the mod.").define("primaryColor", 0xaaaaaa, 0x000000, 0xFFFFFF);
        secondaryColor = config.comment("The secondary color used for the mod.").define("secondaryColor", 0x555555, 0x000000, 0xFFFFFF);
        config.pop();

        config.push("debug");
        isDebug = config.comment("Enable debug mode").define("is_debug", false);
        config.pop();

        config.build();
    }
}
