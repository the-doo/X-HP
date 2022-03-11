package com.doo.xhp.renderer;

import com.doo.xhp.XHP;
import com.doo.xhp.config.XOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;

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
    public int draw(MatrixStack matrixStack, MinecraftClient client, int y, int color, float healScale, VertexConsumerProvider vertexConsumers, int light) {
        return 0;
    }

    public void tips(MatrixStack matrixStack, LivingEntity target) {
        if (!XHP.XOption.tips) {
            return;
        }
        matrixStack.push();

        MinecraftClient mc = MinecraftClient.getInstance();

        float scale = XHP.XOption.tipsScale / 10F;
        matrixStack.scale(scale, scale, scale);

        Text tips = tipsGetter(XHP.XOption.tipsTemplate).apply(target);

        int x = MathHelper.clamp(XHP.XOption.tipsLocation[0], mc.textRenderer.getWidth(tips) / 2, mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(tips) / 2);
        int y = Math.min(XHP.XOption.tipsLocation[1], mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight);

        if (XHP.XOption.tipsMiddle[0]) {
            x = mc.getWindow().getScaledWidth() / 2;
        }
        if (XHP.XOption.tipsMiddle[1]) {
            y = mc.getWindow().getScaledHeight() / 2;
        }

        DrawableHelper.drawCenteredTextWithShadow(matrixStack, mc.textRenderer, tips.asOrderedText(), (int) (x / scale), (int) (y / scale), XHP.XOption.tipsColor);

        // second line
        if (StringUtils.isNotBlank(XHP.XOption.tipsTemplate2)) {
            tips = tipsGetter(XHP.XOption.tipsTemplate2).apply(target);
            DrawableHelper.drawCenteredTextWithShadow(matrixStack, mc.textRenderer, tips.asOrderedText(), (int) (x / scale), (int) (y / scale) + 9, XHP.XOption.tipsColor);
        }

        matrixStack.pop();
    }

    private static Function<LivingEntity, Text> tipsGetter(String temp) {
        return e -> {
            String tips = temp;
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