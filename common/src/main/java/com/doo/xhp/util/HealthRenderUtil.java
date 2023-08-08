package com.doo.xhp.util;

import com.doo.xhp.XHP;
import com.doo.xhp.render.HealRender;
import com.doo.xhp.render.TipHealRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HealthRenderUtil {

    public static final TipHealRender TIP_HEAL_RENDER = new TipHealRender();

    private static HealRender render;

    private static LivingEntity pick;

    private HealthRenderUtil() {
    }

    public static void render(PoseStack poseStack, EntityRenderDispatcher dispatcher, MultiBufferSource bufferSource,
                              LivingEntity living, boolean showName, float baseH, int i) {
        if (XHP.disabled()) {
            cleanPick(living);
            return;
        }

        if (!Minecraft.renderNames() || Minecraft.getInstance().getCameraEntity() == living || living.isInvisible()) {
            cleanPick(living);
            return;
        }

        if (XHP.ignored(living) || dispatcher.distanceToSqr(living) > XHP.distance()) {
            cleanPick(living);
            return;
        }

        boolean staring = isStaring(living);
        if (staring) {
            pick = living;
        } else {
            cleanPick(living);
        }

        if (render == null || XHP.focus() && !staring) {
            return;
        }

        baseH += showName ? 0.35F : 0;
        render.render(poseStack, dispatcher.cameraOrientation(), bufferSource, living, baseH, i);
    }

    private static void cleanPick(LivingEntity living) {
        if (pick == living) {
            pick = null;
        }
    }

    public static boolean isStaring(LivingEntity target) {
        Entity entity = Minecraft.getInstance().cameraEntity;
        if (entity == null) {
            return false;
        }
        // see net.minecraft.world.entity.monster.EnderMan.isLookingAtMe
        Vec3 vec3 = entity.getViewVector(1.0f).normalize();
        Vec3 vec32 = new Vec3(target.getX() - entity.getX(), target.getEyeY() - entity.getEyeY(), target.getZ() - entity.getZ());
        double d = vec32.length();
        double e = vec3.dot(vec32.normalize());
        return e > 1.0 - 0.025 / d;
    }

    public static void setRender(HealRender render) {
        HealthRenderUtil.render = render;
    }

    public static void renderTips(GuiGraphics graphics) {
        if (XHP.disabled() || !TIP_HEAL_RENDER.enabled()) {
            return;
        }

        if (pick == null) {
            return;
        }

        graphics.pose().pushPose();
        Minecraft minecraft = Minecraft.getInstance();
        int x0 = minecraft.getWindow().getGuiScaledWidth() / 2;
        int y0 = minecraft.getWindow().getGuiScaledHeight() / 2;

        TIP_HEAL_RENDER.render(graphics, minecraft.font, pick, x0, y0);

        graphics.pose().popPose();
    }
}
