package com.daqem.questlines.config;

import com.daqem.questlines.Questlines;
import com.daqem.yamlconfig.YamlConfigExpectPlatform;
import com.daqem.yamlconfig.api.config.ConfigExtension;
import com.daqem.yamlconfig.api.config.ConfigType;
import com.daqem.yamlconfig.api.config.IConfigBuilder;
import com.daqem.yamlconfig.impl.config.ConfigBuilder;

import java.util.function.Supplier;

public class QuestlinesConfig {

    public static final Supplier<Integer> primaryColor;
    public static final Supplier<Integer> secondaryColor;

    static {
        IConfigBuilder builder = new ConfigBuilder(
                Questlines.MOD_ID,
                "questlines-common",
                ConfigExtension.YAML,
                ConfigType.COMMON,
                YamlConfigExpectPlatform.getConfigDirectory().resolve(Questlines.MOD_ID)
        );

        builder.push("colors");
        primaryColor = builder.defineInteger("primaryColor", 0xaaaaaa, 0x000000, 0xFFFFFF).withComments("The primary color used for the mod.");
        secondaryColor = builder.defineInteger("secondaryColor", 0x555555, 0x000000, 0xFFFFFF).withComments("The secondary color used for the mod.");
        builder.pop();

        builder.build();
    }

    public static void init() {
    }
}
