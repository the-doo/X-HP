package com.doo.xhp.mixin;

import com.doo.xhp.renderer.HpRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRenderMixin {

    @Inject(at = @At(value = "TAIL"), method = "render")
    private void renderR(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack,
                         VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        Entity camera = MinecraftClient.getInstance().cameraEntity;
        double distance;
        boolean canRender = camera != null && livingEntity.canSee(camera)
                && (distance = camera.getPos().distanceTo(livingEntity.getPos())) > 1
                && distance < 128;
        if (canRender) {
            HpRenderer.render(matrixStack, livingEntity, camera);
        }
    }
}
