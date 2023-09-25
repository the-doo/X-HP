package com.doo.xhp.render;

import com.doo.xhp.enums.HealthTextPosition;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class IconHealRender extends HealRender {
    private static final ResourceLocation C_ID = new ResourceLocation("hud/heart/container_blinking");
    private static final ResourceLocation F_H_ID = new ResourceLocation("hud/heart/absorbing_full");
    private static final ResourceLocation H_ID = new ResourceLocation("hud/heart/full");

    public IconHealRender() {
        position = HealthTextPosition.IGNORED;
        weight = 9;
        height = 9;

        options.addProperty(P_KEY, position.name());
    }

    @Override
    protected boolean needWrapper() {
        return false;
    }

    protected void renderCurrent(GuiGraphics graphics, double process, int endX, int endY, LivingEntity living) {
        RenderSystem.enableDepthTest();
        graphics.blitSprite(C_ID, 0, 0, 9, 9);
        graphics.blitSprite(friendly(living) ? F_H_ID : H_ID, 9, 9, 0, 0, 0, 0, (int) (9 * process), 9);
    }
}
