package com.doo.xhp.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class FenceHealRender extends NameTagLikeHealRender {

    public FenceHealRender() {
        options.remove(TEXT_KEY);
    }

    @Override
    protected void renderContent(GuiGraphics graphics, LivingEntity living, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int backColor = (int) (minecraft.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;

        String back = "||||||||||||||||||||||||||||||||||||||||";

        int len = (int) (living.getHealth() / living.getMaxHealth() * back.length());
        String text = back.substring(0, len);
        float fontX = -font.width(back) / 2F;
        font.drawInBatch(Component.literal(back), fontX, 0, 0x20FFFFFF, false,
                graphics.pose().last().pose(), bufferSource, Font.DisplayMode.SEE_THROUGH, backColor, FONT_LIGHT);
        font.drawInBatch(changeColor(living, Component.literal(text)), fontX, 0, -1, false,
                graphics.pose().last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, FONT_LIGHT);
    }
}
