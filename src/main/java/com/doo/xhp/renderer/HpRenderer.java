package com.doo.xhp.renderer;

import com.doo.xhp.XHP;
import com.doo.xhp.util.HpUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.awt.*;

import static com.doo.xhp.util.HpUtil.BASE_HEIGHT;

public class HpRenderer {

    private static final Identifier HEART_ID =
            new Identifier(XHP.ID, "textures/heart/heart.png");
    private static final Identifier YELLOW_HEART_ID =
            new Identifier(XHP.ID, "textures/heart/yellow_heart.png");
    private static final Identifier EMPTY_HEART_ID =
            new Identifier(XHP.ID, "textures/heart/empty_heart.png");

    public static void render(MatrixStack matrixStack, LivingEntity e, Entity camera) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        if (world == null) {
            return;
        }
        // 基本参数
        int id = e.getEntityId();
        float health = e.getHealth();
        float scale = 0.04F;
        int x = 0;
        int y = (int) (- e.getHeight() * BASE_HEIGHT);
        long time = world.getTime();
        boolean isFriend = !(e instanceof HostileEntity)
                && !HpUtil.isAttacker(id, camera.getEntityId(), time)
                || e.isTeammate(camera);
        int color = isFriend ? Color.ORANGE.getRGB() : Color.RED.getRGB();
        // 矩阵操作
        matrixStack.push();
        // 缩小倍数
        matrixStack.scale(scale, scale, scale);
        // 始终正对玩家
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(camera.yaw - 180 - e.bodyYaw));
        // 画生命值
        if (XHP.option.hp) {
            DrawableHelper.drawCenteredString(
                    matrixStack, client.textRenderer, String.format("%.1f", health), x, y, color);
            y = y - BASE_HEIGHT;
        }
        // 画伤害
        if (XHP.option.damage) {
            HpUtil.get(id).forEach(d -> {
                if (time - d.time > 20) {
                    return;
                }
                DrawableHelper.drawCenteredString(matrixStack, client.textRenderer, String.format("%.1f", d.damage),
                        d.x, d.y, Color.RED.getRGB());
            });
        }
        // 画图片
        if (XHP.option.heart) {
            x = x - HpUtil.HEALTH / 2;
            float healScale = Math.min(health / e.getMaxHealth(), 1);
            TextureManager textureManager = client.getTextureManager();
           // 空血槽
            textureManager.bindTexture(EMPTY_HEART_ID);
            RenderSystem.enableDepthTest();
            DrawableHelper.drawTexture(matrixStack, x, y, 0, 0,
                    HpUtil.HEALTH, HpUtil.HEALTH, HpUtil.HEALTH, HpUtil.HEALTH);
            // 满血槽 * healthScale
            textureManager.bindTexture(isFriend ? YELLOW_HEART_ID : HEART_ID);
            client.gameRenderer.getLightmapTextureManager().enable();
            DrawableHelper.drawTexture(matrixStack, x, y, 0, 0,
                    (int) (healScale * HpUtil.HEALTH), HpUtil.HEALTH, HpUtil.HEALTH, HpUtil.HEALTH);
            RenderSystem.enableDepthTest();
        }
        // 矩阵操作退栈
        matrixStack.pop();
    }
}
