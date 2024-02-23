package com.daqem.questlines.client.gui;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.questline.QuestlineProgress;
import com.daqem.uilib.client.gui.AbstractScreen;
import com.daqem.uilib.client.gui.component.advancement.AdvancementsComponent;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class QuestsScreen extends AbstractScreen {

    private final List<QuestlineProgress> questlineProgresses;

    public QuestsScreen(List<QuestlineProgress> questlineProgresses) {
        super(Questlines.translatable("screen.quests"));
        this.questlineProgresses = questlineProgresses;
    }

    @Override
    public void startScreen() {
        AdvancementsComponent advancementsComponent = new AdvancementsComponent(new ArrayList<>(questlineProgresses));
        advancementsComponent.center();
        this.addComponent(advancementsComponent);
    }

    @Override
    public void onTickScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {

    }
}
