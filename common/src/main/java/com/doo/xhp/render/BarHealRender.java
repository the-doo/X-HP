package com.doo.xhp.render;

import com.doo.xhp.XHP;
import com.doo.xhp.enums.HealthTextGetters;
import com.doo.xhp.enums.HealthTextPosition;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class BarHealRender extends IconHealRender {

    private static final ResourceLocation BACK_ID = new ResourceLocation(XHP.MOD_ID, "textures/bar_back.png");
    private static final ResourceLocation FILL_ID = new ResourceLocation(XHP.MOD_ID, "textures/bar_fill.png");

    private static final int T_WEIGHT = 512;
    private static final int T_HEIGHT = 156;
    private final float xScala;
    private final float yScala;

    public BarHealRender() {
        weight = 80;
        height = 16;
        xScala = 1F * weight / T_WEIGHT;
        yScala = 1F * height / T_HEIGHT;

        position = HealthTextPosition.BOTTOM_RIGHT;

        options.addProperty(P_KEY, position.name());
        options.addProperty(TEXT_KEY, HealthTextGetters.CURRENT_AND_MAX.name());
    }

    protected void renderCurrent(PoseStack posed, double process, int endX, int endY, LivingEntity living) {
        posed.pushPose();
        posed.scale(xScala, yScala, 1);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, FILL_ID);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        GuiComponent.blit(posed, 0, 0, 0, 0, (int) (T_WEIGHT * process), T_HEIGHT, T_WEIGHT, T_HEIGHT);

        RenderSystem.setShaderTexture(0, BACK_ID);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        GuiComponent.blit(posed, 0, 0, 0, 0, T_WEIGHT, T_HEIGHT, T_WEIGHT, T_HEIGHT);
        posed.popPose();
    }
}
