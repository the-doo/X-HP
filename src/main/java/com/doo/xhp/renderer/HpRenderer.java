package com.doo.xhp.renderer;

import com.doo.xhp.XHP;
import com.doo.xhp.config.XOption;
import com.doo.xhp.util.HpUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.util.function.Function;

public class HpRenderer {

    private HpRenderer() {

    }

    private static final Identifier HEART_ID = new Identifier(XHP.ID, "textures/heart/heart.png");
    private static final Identifier YELLOW_HEART_ID = new Identifier(XHP.ID, "textures/heart/yellow_heart.png");
    private static final Identifier EMPTY_HEART_ID = new Identifier(XHP.ID, "textures/heart/empty_heart.png");

    private static final DecimalFormat FORMATTER = new DecimalFormat("#.#");

    public static boolean canRender(LivingEntity entity) {
        if (!XHP.XOption.enabled) {
            return false;
        }

        // option to if hidden hud
        if (XHP.XOption.syncWithHud && MinecraftClient.getInstance().options.hudHidden) {
            return false;
        }

        Entity camera = MinecraftClient.getInstance().cameraEntity;
        if (camera == null) {
            return false;
        }

        if (camera.getPos().distanceTo(entity.getPos()) > XHP.XOption.distance) {
            return false;
        }

        // if FOCUS
        if (XHP.XOption.trigger == XOption.TriggerEnum.FOCUS) {
            return HpUtil.focusTarget(camera) == entity;
        }

        return true;
    }

    public static void render(MatrixStack matrixStack, LivingEntity e) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        Entity camera = client.getCameraEntity();
        if (world == null || camera == null) {
            return;
        }

        // todo filter list
        if (e instanceof ArmorStandEntity && XHP.XOption.ignoreArmorStandEntity) {
            return;
        }

        boolean isDead = e.isDead();
        if (isDead) {
            return;
        }


        // 基本参数
        int id = e.getId();
        float health = e.getHealth();

        float scale = HpUtil.getScale(e.isBaby());
        int y = -HpUtil.getShowY(e.getHeight(), e.isBaby());
        if (e instanceof WitchEntity) {
            y -= 8;
        }
        long time = world.getTime();
        // 判断 --- !(敌对/史莱姆/)
        boolean isFriend = isFriend(e, camera, time);
        int color = isFriend ? XHP.XOption.friendColor : XHP.XOption.mobColor;
        // 矩阵操作
        matrixStack.push();
        // 缩小倍数
        matrixStack.scale(scale, scale, scale);
        // 始终正对玩家
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
        // 翻转
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));
        // 画伤害
        if (XHP.XOption.damage) {
            HpUtil.get(id).stream().filter(d -> time - d.time() <= 20).forEach(d ->
                    DrawableHelper.drawCenteredText(matrixStack, client.textRenderer,
                            String.format("%s", FORMATTER.format(d.damage())), d.x(), -d.y(), d.rgb()));
        }

        // todo if simple tips style
//        if (true) {
//            matrixStack.scale(0.5F, 0.5F, 0.5F);
//            DrawableHelper.drawCenteredText(matrixStack, client.textRenderer, getTipsFormatter().apply(e), 0, (int) (y * 1.8), color);
//            // 矩阵操作退栈
//            matrixStack.pop();
//            return;
//        }

        // 画生命值
        if (XHP.XOption.hp) {
            y -= drawText(matrixStack, client, y, color, String.format("%s", FORMATTER.format(health)));
        }
        // 画图片
        if (XHP.XOption.visualization) {
            float healScale = Math.min(health / e.getMaxHealth(), 1);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            switch (XHP.XOption.style) {
                case BAR -> y -= drawBar(matrixStack, y, color, healScale);
                case ICON -> y -= drawIcon(matrixStack, y, isFriend ? YELLOW_HEART_ID : HEART_ID, healScale);
                case FENCE -> y -= drawFence(matrixStack, client, y, color, healScale);
            }
        }
        // 画名字
        if (XHP.XOption.name) {
            drawText(matrixStack, client, y, color, e.getDisplayName().getString());
        }
        // 矩阵操作退栈
        matrixStack.pop();
    }

    /**
     * 如果不是敌对实体，且目标人物不是玩家，且玩家没有伤害过
     *
     * @param e      渲染实体
     * @param camera 当前摄像机对象
     * @param time   渲染的时间
     * @return 是否
     */
    private static boolean isFriend(LivingEntity e, Entity camera, long time) {
        // 被攻击过
        if (HpUtil.isAttacker(e.getId(), camera.getId(), time)) {
            return false;
        }
        // 如果不是mob or monster
        return !(e instanceof Monster);
    }

    /**
     * 画名字
     *
     * @param matrixStack 矩阵
     * @param client      客户端
     * @param y           y坐标
     * @param color       颜色
     * @param string      名字
     */
    private static int drawText(MatrixStack matrixStack, MinecraftClient client, int y, int color, String string) {
        matrixStack.push();
        matrixStack.scale(0.25F, 0.25F, 0.25F);
        DrawableHelper.drawCenteredText(matrixStack, client.textRenderer, string, 0, (int) (y * 3.5), color);
        matrixStack.pop();
        return client.textRenderer.fontHeight / 2;
    }

    /**
     * 画图标
     *
     * @param matrixStack 矩阵
     * @param y           坐标y
     * @param texture     需要画的icon
     * @param healScale   比例
     * @return height
     */
    private static int drawIcon(MatrixStack matrixStack, int y, Identifier texture, float healScale) {
        matrixStack.push();

        matrixStack.scale(0.5F, 0.5F, 0.5F);

        int x = -HpUtil.HEALTH / 2;
        int healWidth = (int) (healScale * HpUtil.HEALTH);
        // 空血槽
        if (healWidth < HpUtil.HEALTH) {
            RenderSystem.setShaderTexture(0, EMPTY_HEART_ID);
            DrawableHelper.drawTexture(matrixStack, x + healWidth, (int) (y * 1.88), healWidth, 0,
                    HpUtil.HEALTH - healWidth, HpUtil.HEALTH, HpUtil.HEALTH, HpUtil.HEALTH);
        }
        // 血槽
        RenderSystem.setShaderTexture(0, texture);
        DrawableHelper.drawTexture(matrixStack, x, (int) (y * 1.88), 0, 0,
                healWidth, HpUtil.HEALTH, HpUtil.HEALTH, HpUtil.HEALTH);

        matrixStack.pop();
        return (int) (HpUtil.HEALTH / 1.2);
    }

    /**
     * 画血条
     *
     * @param matrixStack 矩阵
     * @param y           坐标y
     * @param color       颜色
     * @param healScale   比例
     * @return height
     */
    private static int drawBar(MatrixStack matrixStack, int y, int color, float healScale) {
        matrixStack.push();

        matrixStack.scale(0.25F, 0.25F, 0.25F);

        // 总长度
        int len = (int) (XHP.XOption.barLength * 3.8);
        // 当前血量长度
        int healLen = (int) (healScale * len);
        int x1 = -(len / 2);
        int x2 = x1 + healLen;
        int y2 = y - XHP.XOption.barHeight;

        DrawableHelper.fill(matrixStack, x1, (int) (y * 3.4), x2, (int) (y2 * 3.4), color);
        if (healLen < len) {
            DrawableHelper.fill(matrixStack, x2, (int) (y * 3.4), x1 + len, (int) (y2 * 3.4), XHP.XOption.emptyColor);
        }

        matrixStack.pop();
        return (int) (XHP.XOption.barHeight * 1.8);
    }

    /**
     * 画栅栏
     *
     * @param matrixStack 矩阵
     * @param client      客户端
     * @param y           坐标y
     * @param color       颜色
     * @param healScale   比例
     * @return height
     */
    private static int drawFence(MatrixStack matrixStack, MinecraftClient client, int y, int color, float healScale) {
        matrixStack.push();
        matrixStack.scale(0.25F, 0.25F, 0.25F);

        String fenceStr = "||||||||||||||||||||";
        int len = (int) (fenceStr.length() * healScale);
        LiteralText heal = new LiteralText(fenceStr.substring(0, len));
        LiteralText empty = new LiteralText(fenceStr.substring(len));
        heal.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
        empty.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(XHP.XOption.emptyColor)));

        DrawableHelper.drawCenteredText(matrixStack, client.textRenderer, heal.append(empty), 0, (int) (y * 3.5), color);

        matrixStack.pop();

        return client.textRenderer.fontHeight / 2;
    }

    public static void renderTips(MatrixStack matrixStack, LivingEntity target) {
        if (!XHP.XOption.tips) {
            return;
        }
        matrixStack.push();

        MinecraftClient mc = MinecraftClient.getInstance();

        Text tips = getTipsFormatter().apply(target);

        int x = Math.min(XHP.XOption.tipsLocation[0], mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(tips));
        int y = Math.min(XHP.XOption.tipsLocation[1], mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight);
        DrawableHelper.drawTextWithShadow(matrixStack, mc.textRenderer, tips, x, y, XHP.XOption.tipsColor);

        matrixStack.pop();
    }

    private static Function<LivingEntity, Text> getTipsFormatter() {
        String format = ": %s / %s - Armor(%d)";
        return e -> LiteralText.EMPTY.copy().append(e.getDisplayName())
                .append(String.format(format, FORMATTER.format(e.getHealth()), FORMATTER.format(e.getMaxHealth()), e.getArmor()));
    }
}
