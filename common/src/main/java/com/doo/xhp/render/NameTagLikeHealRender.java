package com.doo.xhp.render;

import com.doo.xhp.enums.HealthRenders;
import com.doo.xhp.enums.HealthTextGetters;
import com.doo.xhp.enums.MenuOptType;
import com.doo.xhp.interfaces.WithOption;
import com.doo.xhp.screen.MenuScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;

public class NameTagLikeHealRender extends HealRender {

    protected static final String TEXT_KEY = "text_type";

    public NameTagLikeHealRender() {
        height = 9;

        options.addProperty(TEXT_KEY, HealthTextGetters.PERCENTAGE.name());
    }

    @Override
    public void registerOpt() {
        super.registerOpt();

        MenuScreen.register(MenuOptType.ENUM, HealthRenders.NAME_TAG.name(), TEXT_KEY, HealthTextGetters.class);
    }

    @Override
    protected boolean needMoveCenter() {
        return false;
    }

    @Override
    protected boolean needWrapper() {
        return false;
    }

    protected int renderContent(PoseStack graphics, LivingEntity living, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int backColor = (int) (minecraft.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;

        String text = WithOption.enumV(options, TEXT_KEY, HealthTextGetters.class)
                .orElse(HealthTextGetters.PERCENTAGE).formatted(living, "%s/%s");

        float fontX = -font.width(text) / 2F;
        Component component = changeColor(living, new TextComponent(text));
        font.drawInBatch(component, fontX, 0, 0x20FFFFFF, false,
                graphics.last().pose(), bufferSource, true, backColor, FONT_LIGHT);
        font.drawInBatch(component, fontX, 0, -1, false,
                graphics.last().pose(), bufferSource, false, 0, FONT_LIGHT);
        return -(int) fontX;
    }

    protected Component changeColor(LivingEntity living, MutableComponent component) {
        float v = living.getHealth() / living.getMaxHealth();
        if (v < 0.35) {
            return component.withStyle(ChatFormatting.RED);
        }
        if (v < 0.65) {
            return component.withStyle(ChatFormatting.YELLOW);
        }

        return component.withStyle(ChatFormatting.GREEN);
    }
}
