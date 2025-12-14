package com.daqem.questlines.client.gui;

import com.daqem.questlines.Questlines;
import com.daqem.questlines.questline.QuestlineProgress;
import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.background.DarkenedBackground;
import com.daqem.uilib.gui.component.skilltree.SkillTreeComponent;
import com.daqem.uilib.gui.widget.ButtonWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class QuestsScreen extends AbstractScreen {

    private final List<QuestlineProgress> questlineProgresses;
    private int activeIndex = 0;

    public QuestsScreen(List<QuestlineProgress> questlineProgresses) {
        super(Questlines.translatable("screen.quests"));
        this.questlineProgresses = questlineProgresses;
        this.setBackground(new DarkenedBackground());
    }

    @Override
    protected void init() {
        this.clear();
        super.init();

        if (questlineProgresses.isEmpty()) {
            this.addRenderableWidget(new ButtonWidget(
                    (this.width - 200) / 2,
                    this.height / 2,
                    200, 20,
                    Component.literal("No Questlines Available")
            ));
            return;
        }

        // Ensure index is valid
        this.activeIndex = Mth.clamp(activeIndex, 0, questlineProgresses.size() - 1);
        QuestlineProgress activeQuestline = questlineProgresses.get(activeIndex);

        // Add the Skill Tree Component
        // We leave space at the top/bottom for navigation buttons/labels
        int treeWidth = this.width - 40;
        int treeHeight = this.height - 60;

        SkillTreeComponent currentTreeComponent = new SkillTreeComponent(
                20,
                40,
                treeWidth,
                treeHeight,
                activeQuestline
        );
        this.addComponent(currentTreeComponent);

        // Navigation Buttons
        this.addWidget(new ButtonWidget(
                10,
                10,
                20,
                20,
                Component.literal("<"),
                b -> previousQuestline()
        ));

        this.addWidget(new ButtonWidget(
                this.width - 30,
                10,
                20,
                20,
                Component.literal(">"),
                b -> nextQuestline()
        ));
    }

    private void nextQuestline() {
        if (questlineProgresses.size() > 1) {
            activeIndex = (activeIndex + 1) % questlineProgresses.size();
            rebuildScreen();
        }
    }

    private void previousQuestline() {
        if (questlineProgresses.size() > 1) {
            activeIndex = (activeIndex - 1 + questlineProgresses.size()) % questlineProgresses.size();
            rebuildScreen();
        }
    }

    private void rebuildScreen() {
        this.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (!questlineProgresses.isEmpty()) {
            Component title = questlineProgresses.get(activeIndex).getName();
            guiGraphics.drawCenteredString(this.font, title, this.width / 2, 16, 0xFFFFFFFF);
        }
    }
}