package com.doo.xhp.renderer;

import com.doo.xhp.XHP;
import com.doo.xhp.config.XOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

import java.util.function.Function;

public class TipsRenderer implements HpRenderer {

    public static final TipsRenderer INSTANCE = new TipsRenderer();

    private TipsRenderer() {
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
    public int draw(MatrixStack matrixStack, MinecraftClient client, int y, int color, float healScale, VertexConsumerProvider vertexConsumers) {
        return 0;
    }

    public void tips(MatrixStack matrixStack, LivingEntity target) {
        if (!XHP.XOption.tips) {
            return;
        }
        matrixStack.push();

        MinecraftClient mc = MinecraftClient.getInstance();

        Text tips = tipsGetter().apply(target);

        int x = Math.min(XHP.XOption.tipsLocation[0], mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(tips));
        int y = Math.min(XHP.XOption.tipsLocation[1], mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight);

        if (Math.abs(mc.getWindow().getScaledWidth() / 2 - XHP.XOption.tipsLocation[0]) < 10) {
            x = (mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(tips)) / 2;
        }

        DrawableHelper.drawTextWithShadow(matrixStack, mc.textRenderer, tips, x, y, XHP.XOption.tipsColor);

        matrixStack.pop();
    }

    private static Function<LivingEntity, Text> tipsGetter() {
        return e -> {
            String tips = XHP.XOption.tipsTemplate;
            if (e != null && tips != null && tips.length() > 0) {
                // replace all tips
                for (XOption.AttrKeyValue kv : XOption.AttrKeyValue.values()) {
                    tips = tips.replaceAll(kv.key, kv.valueGetter.apply(e));
                }
            }
            return Text.of(tips);
        };
    }
}
