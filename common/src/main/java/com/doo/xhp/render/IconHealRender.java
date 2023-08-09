package com.doo.xhp.render;

import com.doo.xhp.enums.HealthTextPosition;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class IconHealRender extends HealRender {

    private static final ResourceLocation ID = new ResourceLocation("textures/gui/icons.png");
    private static final int CONTAINER_IDX = 43;
    private static final int MOB_IDX = 52;
    private static final int FRIENDLY_IDX = 52 + 9 * 12;

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

    protected void renderCurrent(PoseStack graphics, double process, int endX, int endY, LivingEntity living) {
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, ID);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        GuiComponent.blit(graphics, 0, 0, CONTAINER_IDX, 0, 9, 9);
        GuiComponent.blit(graphics, 0, 0, friendly(living) ? FRIENDLY_IDX : MOB_IDX, 0, (int) (9 * process), 9);
    }
}
