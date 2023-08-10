package com.doo.xhp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;

public class FenceHealRender extends NameTagLikeHealRender {

    public FenceHealRender() {
        options.remove(TEXT_KEY);
    }

    @Override
    protected int renderContent(PoseStack graphics, LivingEntity living, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int backColor = (int) (minecraft.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;

        String back = "||||||||||||||||||||||||||||||||||||||||";

        int len = (int) (living.getHealth() / living.getMaxHealth() * back.length());
        String text = back.substring(0, len);
        float fontX = -font.width(back) / 2F;
        font.drawInBatch(new TextComponent(back), fontX, 0, 0x20FFFFFF, false,
                graphics.last().pose(), bufferSource, true, backColor, FONT_LIGHT);
        font.drawInBatch(changeColor(living, new TextComponent(text)), fontX, 0, -1, false,
                graphics.last().pose(), bufferSource, false, 0, FONT_LIGHT);
        return -(int) fontX;
    }
}
