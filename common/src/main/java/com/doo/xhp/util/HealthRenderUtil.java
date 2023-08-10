package com.doo.xhp.util;

import com.doo.xhp.XHP;
import com.doo.xhp.enums.HealthRenders;
import com.doo.xhp.render.HealRender;
import com.doo.xhp.render.ImageHealRender;
import com.doo.xhp.render.TipHealRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
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
                              LivingEntity living, float baseY, int i) {
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

        if (render == null || !render.enabled() || XHP.focus() && !staring) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0f, baseY, 0.0f);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.scale(-0.025f, -0.025f, 0.025f);

        render.render(poseStack, bufferSource, living);

        poseStack.popPose();
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

    public static void renderTips(PoseStack graphics) {
        if (XHP.disabled() || !TIP_HEAL_RENDER.enabled()) {
            return;
        }

        if (pick == null) {
            return;
        }

        graphics.pushPose();
        Minecraft minecraft = Minecraft.getInstance();
        int x0 = minecraft.getWindow().getGuiScaledWidth() / 2;
        int y0 = minecraft.getWindow().getGuiScaledHeight() / 2;

        TIP_HEAL_RENDER.render(graphics, minecraft.font, pick, x0, y0);

        graphics.popPose();
    }

    public static void onClientStarted(Minecraft client) {
        ((ImageHealRender) HealthRenders.IMAGE.getRender()).reloadImage(client);
    }
}
