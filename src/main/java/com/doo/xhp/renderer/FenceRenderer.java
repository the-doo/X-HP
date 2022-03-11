package com.doo.xhp.renderer;

import com.doo.xhp.XHP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;

public class FenceRenderer implements HpRenderer {

    private static final String BASE = "||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||";

    public static final FenceRenderer INSTANCE = new FenceRenderer();

    @Override
    public int getHeight() {
        return MinecraftClient.getInstance().textRenderer.fontHeight;
    }

    @Override
    public int getWidth() {
        int baseWid = MinecraftClient.getInstance().textRenderer.getWidth("|");
        return XHP.XOption.barLength / baseWid * baseWid;
    }

    @Override
    public int getMarginY() {
        return 0;
    }

    @Override
    public int draw(MatrixStack matrixStack, MinecraftClient client, int y, int color, float healScale, VertexConsumerProvider vertexConsumers, int light) {
        matrixStack.push();

        int baseWid = MinecraftClient.getInstance().textRenderer.getWidth("|");
        String fenceStr = BASE.substring(0, XHP.XOption.barLength / baseWid);
        int len = (int) (fenceStr.length() * healScale);

        LiteralText heal = new LiteralText(fenceStr.substring(0, len));
        LiteralText empty = new LiteralText(fenceStr.substring(len));
        heal.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
        empty.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(XHP.XOption.emptyColor)));

        HpRenderer.drawText(matrixStack, client, 0, y, color, heal.append(empty), vertexConsumers);

        matrixStack.pop();

        return client.textRenderer.fontHeight;
    }
}
