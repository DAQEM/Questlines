package com.daqem.questlines.client;

import com.daqem.questlines.client.event.KeyPressedEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class QuestlinesClient {

    private static final String QUESTLINES_CATEGORY = "key.categories.questlines";
    public static final KeyMapping OPEN_QUEST_SCREEN = new KeyMapping("key.challenges.open_quest_screen", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Q, QUESTLINES_CATEGORY);

    public static void init() {
        registerEvents();
    }

    private static void registerEvents() {
        KeyPressedEvent.registerEvent();
    }

}
