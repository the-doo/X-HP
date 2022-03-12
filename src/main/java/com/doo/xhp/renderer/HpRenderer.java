package com.doo.xhp.renderer;

import com.doo.xhp.XHP;
import com.doo.xhp.util.HpUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
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
            renderLayer(matrices, entity, hasLabel, vertexConsumers, light);
        }

        matrices.pop();
    }

    static void renderLayer(MatrixStack matrixStack, LivingEntity e, boolean hasLabel, VertexConsumerProvider vertexConsumers, int light) {
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
        int color = HpUtil.getColor(e, camera);

        int y = hasLabel ? -client.textRenderer.fontHeight : 0;

        matrixStack.push();

        if (XHP.XOption.seeThrough) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
        }

        // hp
        if (XHP.XOption.hp && !XHP.XOption.oneLine) {
            y -= drawText(matrixStack, client, 0, y, color, Text.of(HpUtil.FORMATTER.format(health)), vertexConsumers);
            y -= 3;
        }

        // icon
        if (XHP.XOption.visualization) {
            float healScale = Math.min(health / e.getMaxHealth(), 1);
            y -= XHP.XOption.style.layout.draw(matrixStack, client, y, color, healScale, vertexConsumers, light);
            y -= 3;
        }

        // name
        if (XHP.XOption.name && !XHP.XOption.oneLine) {
            y -= drawText(matrixStack, client, 0, y, color, e.getDisplayName(), vertexConsumers);
            y -= 3;
        }

        // one line
        if (XHP.XOption.oneLine) {
            drawOneLine(matrixStack, client, y, color, XHP.XOption.name ? e.getDisplayName() : null, XHP.XOption.hp ? Text.of(HpUtil.FORMATTER.format(health)) : null, vertexConsumers);
        }

        matrixStack.pop();
    }

    static void drawOneLine(MatrixStack matrixStack, MinecraftClient client, int y, int color, Text name, Text hp, VertexConsumerProvider vertexConsumers) {
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
            drawText(matrixStack, client, -width / 2 + nameW / 2, y, color, name, vertexConsumers);
        }

        if (hp != null) {
            drawText(matrixStack, client, hpX, hpY, color, hp, vertexConsumers);
        }

        matrixStack.pop();
    }

    static int drawText(MatrixStack matrixStack, MinecraftClient client, int x, int y, int color, Text text, VertexConsumerProvider vertexConsumers) {
        matrixStack.push();

        boolean canSee = XHP.XOption.seeThrough;
        client.textRenderer.draw(text, (float) (x - client.textRenderer.getWidth(text) / 2), y, color, true, matrixStack.peek().getPositionMatrix(), vertexConsumers, canSee, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);

        matrixStack.pop();
        return client.textRenderer.fontHeight;
    }

    int draw(MatrixStack matrixStack, MinecraftClient client, int y, int color, float healScale, VertexConsumerProvider vertexConsumers, int light);
}
