package com.daqem.questlines.client.gui.widget;

import com.daqem.questlines.questline.quest.QuestProgress;
import com.daqem.uilib.api.skilltree.ISkillTreeItem;
import com.daqem.uilib.api.widget.skilltree.ISkillTreeItemWidget;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class QuestWidget extends CustomButtonWidget implements ISkillTreeItemWidget {

    private final QuestProgress questProgress;

    // Standard advancement frames
    private static final WidgetSprites TASK_FRAME_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("advancements/task_frame_unobtained"),
            ResourceLocation.withDefaultNamespace("advancements/task_frame_obtained"),
            ResourceLocation.withDefaultNamespace("advancements/task_frame_unobtained"), // Focused
            ResourceLocation.withDefaultNamespace("advancements/task_frame_obtained")  // Focused
    );

    public QuestWidget(QuestProgress questProgress) {
        super(0, 0, 26, 26, Component.empty(), TASK_FRAME_SPRITES, button -> {
            // Handle quest click if needed (e.g., show details)
        });
        this.questProgress = questProgress;
        this.active = questProgress.isCompleted();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        // Render the quest icon centered in the frame
        guiGraphics.renderFakeItem(questProgress.getIcon(), this.getX() + 5, this.getY() + 5);
        // If the item has decorations, render them
        guiGraphics.renderItemDecorations(
                Minecraft.getInstance().font,
                questProgress.getIcon(),
                this.getX() + 5,
                this.getY() + 5
        );
    }

    @Override
    public void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw standard advancement tooltip box
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                ResourceLocation.withDefaultNamespace("advancements/title_box"),
                getX(),
                getY() - 30, // Position above the widget
                getWidth() + 100, // Arbitrary width for title
                26
        );

        // Draw the name inside the tooltip box
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                questProgress.getName(),
                getX() + 5,
                getY() - 22,
                0xFFFFFFFF,
                true
        );

        // Use standard screen tooltip rendering for the description
        if (isMouseOver(mouseX, mouseY)) {
            guiGraphics.setComponentTooltipForNextFrame(
                    Minecraft.getInstance().font,
                    questProgress.getDescription(),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public ISkillTreeItem getSkillTreeItem() {
        return questProgress;
    }
}