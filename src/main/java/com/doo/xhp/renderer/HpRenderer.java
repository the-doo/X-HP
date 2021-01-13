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
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.awt.*;

public class HpRenderer {

    private static final Identifier HEART_ID = new Identifier(XHP.ID, "textures/heart/heart.png");
    private static final Identifier YELLOW_HEART_ID = new Identifier(XHP.ID, "textures/heart/yellow_heart.png");
    private static final Identifier EMPTY_HEART_ID = new Identifier(XHP.ID, "textures/heart/empty_heart.png");

    private static final int[] COLORS = {Color.RED.getRGB(), Color.ORANGE.getRGB(), Color.DARK_GRAY.getRGB()};

    public static void render(MatrixStack matrixStack, LivingEntity e, Entity camera) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        if (world == null) {
            return;
        }
        // 基本参数
        int id = e.getEntityId();
        float health = e.getHealth();
        float scale = HpUtil.getScale(e.isBaby());
        int y = -HpUtil.getShowY(e.getHeight(), e.isBaby());
        long time = world.getTime();
        boolean isFriend = !(e instanceof HostileEntity)
                && !HpUtil.isAttacker(id, camera.getEntityId(), time)
                || e.isTeammate(camera);
        int color = isFriend ? COLORS[1] : COLORS[0];
        // 矩阵操作
        matrixStack.push();
        // 缩小倍数
        matrixStack.scale(scale, scale, scale);
        // 始终正对玩家
        matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-camera.yaw));
        // 翻转
        matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180));
        // 画生命值
        boolean isIcon = XHP.option.isIcon();
        if (XHP.option.hp) {
            DrawableHelper.drawCenteredString(
                    matrixStack, client.textRenderer, String.format("%.1f", health), 0, y, color);
            y = (int) (y - HpUtil.BASE_HEIGHT / (isIcon ? 1 : 2.5));
        }
        // 画伤害
        if (XHP.option.damage) {
            HpUtil.get(id).forEach(d -> {
                if (time - d.time > 20) {
                    return;
                }
                DrawableHelper.drawCenteredString(matrixStack, client.textRenderer, String.format("%.1f", d.damage),
                        d.x, -d.y, d.rgb);
            });
        }
        // 画图片
        if (XHP.option.heart) {
            float healScale = Math.min(health / e.getMaxHealth(), 1);
            RenderSystem.enableDepthTest();
            if (isIcon) {
                drawIcon(matrixStack, client, y, isFriend ? YELLOW_HEART_ID : HEART_ID, healScale);
            } else if (XHP.option.isBar()) {
                drawBar(matrixStack, y, color, healScale);
            } else {
                drawFence(matrixStack, client, y, color, healScale);
            }
            RenderSystem.disableDepthTest();
        }
        // 矩阵操作退栈
        matrixStack.pop();
    }

    /**
     * 画图标
     *
     * @param matrixStack 矩阵
     * @param client      客户端
     * @param y           坐标y
     * @param texture     需要画的icon
     * @param healScale   比例
     */
    private static void drawIcon(MatrixStack matrixStack, MinecraftClient client, int y, Identifier texture, float healScale) {
        int x = -HpUtil.HEALTH / 2;
        int healWidth = (int) (healScale * HpUtil.HEALTH);
        TextureManager textureManager = client.getTextureManager();
        // 空血槽
        if (healWidth < HpUtil.HEALTH) {
            textureManager.bindTexture(EMPTY_HEART_ID);
            DrawableHelper.drawTexture(matrixStack, x + healWidth, y, healWidth, 0,
                    HpUtil.HEALTH - healWidth, HpUtil.HEALTH, HpUtil.HEALTH, HpUtil.HEALTH);
        }
        // 血槽
        textureManager.bindTexture(texture);
        DrawableHelper.drawTexture(matrixStack, x, y, 0, 0,
                healWidth, HpUtil.HEALTH, HpUtil.HEALTH, HpUtil.HEALTH);
    }

    /**
     * 画血条
     *
     * @param matrixStack 矩阵
     * @param y           坐标y
     * @param color       颜色
     * @param healScale   比例
     */
    private static void drawBar(MatrixStack matrixStack, int y, int color, float healScale) {
        // 总长度
        int len = XHP.option.barLength;
        // 当前血量长度
        int healLen = (int) (healScale * len);
        int x1 = -(len / 2);
        int x2 = x1 + healLen;
        int y2 = y - XHP.option.barHeight;
        DrawableHelper.fill(matrixStack, x1, y, x2, y2, color);
        if (healLen < len) {
            DrawableHelper.fill(matrixStack, x2, y, x1 + len, y2, COLORS[2]);
        }
    }

    /**
     * 画栅栏
     *
     * @param matrixStack 矩阵
     * @param client      客户端
     * @param y           坐标y
     * @param color       颜色
     * @param healScale   比例
     */
    private static void drawFence(MatrixStack matrixStack, MinecraftClient client, int y, int color, float healScale) {
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        String fenceStr = "||||||||||||||||||||";
        int len = (int) (fenceStr.length() * healScale);
        LiteralText heal = new LiteralText(fenceStr.substring(0, len));
        LiteralText empty = new LiteralText(fenceStr.substring(len));
        heal.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
        empty.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color.DARK_GRAY.getRGB())));
        DrawableHelper.drawCenteredText(matrixStack, client.textRenderer, heal.append(empty), 0, y * 2, color);
        matrixStack.scale(2, 2, 2);
    }
}
