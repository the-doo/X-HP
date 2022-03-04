package com.doo.xhp.renderer;

import com.doo.xhp.XHP;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class IconRenderer implements HpRenderer {

    private static final Identifier HEART_ID = new Identifier(XHP.ID, "textures/heart/heart.png");
    private static final Identifier YELLOW_HEART_ID = new Identifier(XHP.ID, "textures/heart/yellow_heart.png");
    private static final Identifier EMPTY_HEART_ID = new Identifier(XHP.ID, "textures/heart/empty_heart.png");

    public static final IconRenderer INSTANCE = new IconRenderer();

    private final int width = 16;

    @Override
    public int getHeight() {
        return width;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getMarginY() {
        return 0;
    }

    @Override
    public int draw(MatrixStack matrixStack, MinecraftClient client, int y, int color, float healScale, VertexConsumerProvider vertexConsumers, int light) {

        matrixStack.push();

        y = -3 - width;

        int x = -width / 2;
        int healWidth = (int) (healScale * width);
        // 空血槽
        if (healWidth < width) {
            RenderSystem.setShaderTexture(0, EMPTY_HEART_ID);
            DrawableHelper.drawTexture(matrixStack, x + healWidth, y, healWidth, 0, width - healWidth, width, width, width);
        }
        // 血槽
        RenderSystem.setShaderTexture(0, color == XHP.XOption.friendColor ? YELLOW_HEART_ID : HEART_ID);
        DrawableHelper.drawTexture(matrixStack, x, y, 0, 0, healWidth, width, width, width);

        matrixStack.pop();
        return width;
    }
}
