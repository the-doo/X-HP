package com.doo.xhp.renderer;

import com.doo.xhp.XHP;
import com.doo.xhp.util.HpUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Just render
 */
public interface HpRenderer {

    /**
     * 样式枚举
     */
    enum BarStyleEnum {
        BAR("xhp.menu.style.bar", BarRenderer.INSTANCE),
        ICON("xhp.menu.style.icon", IconRenderer.INSTANCE),
        FENCE("xhp.menu.style.fence", FenceRenderer.INSTANCE);

        public final String key;
        public final HpRenderer layout;

        BarStyleEnum(String key, HpRenderer layout) {
            this.key = key;
            this.layout = layout;
        }

        public static BarStyleEnum get(Integer index) {
            return values()[index % values().length];
        }
    }

    int getHeight();

    int getWidth();

    int getMarginY();

    static void render(MatrixStack matrices, LivingEntity entity, VertexConsumerProvider vertexConsumers, int light, TextRenderer textRenderer, EntityRenderDispatcher dispatcher, boolean hasLabel) {
        if (!HpUtil.mustCheck(entity)) {
            return;
        }

        float f = entity.getHeight() + 0.5f;

        matrices.push();
        matrices.translate(0.0, f, 0.0);
        matrices.multiply(dispatcher.getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        boolean can = HpUtil.canRender(entity);

        // draw damage
        if (XHP.XOption.damage && (!XHP.XOption.damageFollow || can)) {
            DamageRenderer.INSTANCE.drawDamage(matrices, textRenderer, entity, vertexConsumers, light);
        }

        // draw other info
        if (can) {
            render(matrices, entity);
        }

        matrices.pop();
    }

    static void render(MatrixStack matrixStack, LivingEntity e) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        Entity camera = client.getCameraEntity();
        if (world == null || camera == null) {
            return;
        }

        boolean isDead = e.isDead();
        if (isDead) {
            return;
        }

        float health = e.getHealth();
        int color = HpUtil.isFriend(e, camera) ? XHP.XOption.friendColor : XHP.XOption.mobColor;
        int y = 0;

        matrixStack.push();

        // hp
        if (XHP.XOption.hp && !XHP.XOption.oneLine) {
            y -= drawText(matrixStack, client, y, color, Text.of(HpUtil.FORMATTER.format(health)));
            y -= 3;
        }

        // icon
        if (XHP.XOption.visualization) {
            float healScale = Math.min(health / e.getMaxHealth(), 1);
            y -= XHP.XOption.style.layout.draw(matrixStack, client, y, color, healScale);
            y -= 3;
        }

        // name
        if (XHP.XOption.name && !XHP.XOption.oneLine) {
            drawText(matrixStack, client, y, color, e.getDisplayName());
        }

        // one line
        if (XHP.XOption.oneLine) {
            drawOneLine(matrixStack, client, y, color, XHP.XOption.name ? e.getDisplayName() : null, XHP.XOption.hp ? Text.of(HpUtil.FORMATTER.format(health)) : null);
        }

        matrixStack.pop();
    }

    static void drawOneLine(MatrixStack matrixStack, MinecraftClient client, int y, int color, Text name, Text hp) {
        if (name == null && hp == null) {
            return;
        }

        matrixStack.push();

        int width = XHP.XOption.barLength;

        if (XHP.XOption.style == BarStyleEnum.FENCE) {
            width = BarStyleEnum.FENCE.layout.getWidth();
        } else if (XHP.XOption.style == BarStyleEnum.ICON) {
            y *= 1.5;
            y -= 3;
        }

        // check max width
        int nameW = name == null ? 0 : client.textRenderer.getWidth(name);
        int hpW = hp == null ? 0 : client.textRenderer.getWidth(hp);

        int hpX = width / 2 - hpW / 2;
        int hpY = y;
        if (nameW + hpW + 3 > width) {
            // need display at name next line
            hpY -= (client.textRenderer.fontHeight + 3);
            hpX = -width / 2 + hpW / 2;
        }

        if (name != null) {
            DrawableHelper.drawCenteredText(matrixStack, client.textRenderer, name, -width / 2 + nameW / 2, y, color);
        }

        if (hp != null) {
            DrawableHelper.drawCenteredText(matrixStack, client.textRenderer, hp, hpX, hpY, color);
        }

        matrixStack.pop();
    }

    /**
     * 画名字
     *
     * @param matrixStack 矩阵
     * @param client      客户端
     * @param y           y坐标
     * @param color       颜色
     * @param text        名字
     */
    private static int drawText(MatrixStack matrixStack, MinecraftClient client, int y, int color, Text text) {
        matrixStack.push();

        DrawableHelper.drawCenteredText(matrixStack, client.textRenderer, text, 0, y, color);

        matrixStack.pop();
        return client.textRenderer.fontHeight;
    }

    int draw(MatrixStack matrixStack, MinecraftClient client, int y, int color, float healScale);
}
