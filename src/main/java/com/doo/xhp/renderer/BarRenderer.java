package com.doo.xhp.renderer;

import com.doo.xhp.XHP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class BarRenderer implements HpRenderer {

    public static final BarRenderer INSTANCE = new BarRenderer();

    @Override
    public int getHeight() {
        return XHP.XOption.barHeight;
    }

    @Override
    public int getWidth() {
        return XHP.XOption.barLength;
    }

    @Override
    public int getMarginY() {
        return 0;
    }

    @Override
    public int draw(MatrixStack matrixStack, MinecraftClient client, int y, int color, float healScale, VertexConsumerProvider vertexConsumers) {
        matrixStack.push();

        int height = XHP.XOption.barHeight;

        y = y != 0 ? -3 : height;

        int len = XHP.XOption.barLength;
        int healLen = (int) (healScale * len);

        int x = -(len / 2);

        DrawableHelper.fill(matrixStack, x, y, x + healLen, y - height, color);
        DrawableHelper.fill(matrixStack, x + healLen, y, x + len, y - height, XHP.XOption.emptyColor);

        matrixStack.pop();
        return height;
    }
}
