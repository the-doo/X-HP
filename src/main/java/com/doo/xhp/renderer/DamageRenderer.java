package com.doo.xhp.renderer;

import com.doo.xhp.XHP;
import com.doo.xhp.interfaces.Damageable;
import com.doo.xhp.util.HpUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Matrix4f;

import java.awt.*;
import java.util.List;

public class DamageRenderer implements HpRenderer {

    public static final DamageRenderer INSTANCE = new DamageRenderer();

    private DamageRenderer() {
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getMarginY() {
        return 0;
    }

    @Override
    public int draw(MatrixStack matrixStack, MinecraftClient client, int y, int color, float healScale, VertexConsumerProvider vertexConsumers, int light) {
        return 0;
    }

    public void drawDamage(MatrixStack matrices, TextRenderer textRenderer, LivingEntity entity, VertexConsumerProvider vertexConsumers, int light) {
        if (!(entity instanceof Damageable)) {
            return;
        }
        matrices.push();

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        List<HpUtil.DamageR> damageRS = ((Damageable) entity).getDamageList();
        long time = entity.world.getTime();
        damageRS.stream().filter(d -> time - d.time() <= 30).forEach(d -> {
            long t = time - d.time();
            float scale = XHP.XOption.damageScale / 10F + Math.min(1F, 6F / t);
            if (t < 6) {
                scale = XHP.XOption.damageScale / 10F + Math.min(1F, t / 5F);
            }

            matrices.scale(scale, scale, scale);

            int color = XHP.XOption.damageColor;
            if (d.damage() > 0) {
                color = Color.GREEN.getRGB();
            } else if (d.isCrit()) {
                color = XHP.XOption.criticDamageColor;
            }

            int speed = 2;
            float x = t * d.x() * speed;
            float y = -t * d.y() * speed + (XHP.XOption.damageFromMiddle ? entity.getHeight() * 20F : 0);
            textRenderer.draw(HpUtil.FORMATTER.format(Math.abs(d.damage())), x / scale, y / scale, color, false, matrix4f, vertexConsumers, true, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        });

        matrices.pop();
    }
}
